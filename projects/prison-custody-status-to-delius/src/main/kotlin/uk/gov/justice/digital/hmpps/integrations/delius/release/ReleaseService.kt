package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.FeatureFlagCodes.HOSPITAL_RELEASE
import uk.gov.justice.digital.hmpps.FeatureFlagCodes.RELEASE_ETL23
import uk.gov.justice.digital.hmpps.MovementReasonCodes
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datasource.OptimisationContext
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.custody.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.CustodyService
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService
import uk.gov.justice.digital.hmpps.integrations.delius.event.manager.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.manager.getByEventId
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.host.HostRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.getByNomisCdeCode
import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallService
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.getByCodeAndSelectableIsTrue
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getReleaseType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.CUSTODY_ROTL
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.IN_CUSTODY
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.RECALLED
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.RELEASED_ON_LICENCE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.RELEASABLE_STATUSES
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseOutcome.MultipleEventsReleased
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseOutcome.PrisonerDied
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseOutcome.PrisonerReleased
import java.time.LocalDate.now
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS

const val ETL23_NOTES = "This is a ROTL release on Extended Temporary Licence (ETL23)"

enum class ReleaseOutcome {
    MultipleEventsReleased,
    PrisonerReleased,
    PrisonerDied
}

@Service
class ReleaseService(
    auditedInteractionService: AuditedInteractionService,
    private val referenceDataRepository: ReferenceDataRepository,
    private val institutionRepository: InstitutionRepository,
    private val hostRepository: HostRepository,
    private val eventService: EventService,
    private val releaseRepository: ReleaseRepository,
    private val custodyService: CustodyService,
    private val orderManagerRepository: OrderManagerRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val personDied: PersonDied,
    private val recallReasonRepository: RecallReasonRepository,
    private val recallService: RecallService,
    private val featureFlags: FeatureFlags
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun release(
        nomsNumber: String,
        prisonId: String?,
        reason: String,
        movementReasonCode: String,
        releaseDateTime: ZonedDateTime
    ) = if (movementReasonCode == MovementReasonCodes.DIED) {
        personDied.inCustody(nomsNumber, releaseDateTime)
        PrisonerDied
    } else {
        val releaseType = mapToReleaseType(reason, movementReasonCode)?.let {
            referenceDataRepository.getReleaseType(it.code)
        }
        val institution = lazy {
            prisonId?.let { institutionRepository.getByNomisCdeCode(it) }
                ?: institutionRepository.getByCode(InstitutionCode.OTHER_SECURE_UNIT.code)
        }

        val events = eventService.getActiveCustodialEvents(nomsNumber)
        events.forEach {
            addReleaseToEvent(
                it,
                institution,
                releaseType,
                releaseDateTime,
                movementReasonCode
            )
        }
        if (events.size > 1) MultipleEventsReleased else PrisonerReleased
    }

    private fun addReleaseToEvent(
        event: Event,
        lazyInstitution: Lazy<Institution>,
        releaseType: ReferenceData?,
        releaseDateTime: ZonedDateTime,
        movementReasonCode: String
    ): Unit = audit(BusinessInteractionCode.ADD_RELEASE) { audit ->
        audit["eventId"] = event.id
        OptimisationContext.offenderId.set(event.person.id)

        val disposal = event.disposal ?: throw NotFoundException("Disposal", "eventId", event.id)
        val custody = disposal.custody ?: throw NotFoundException("Custody", "disposalId", disposal.id)
        val releaseDate = releaseDateTime.truncatedTo(DAYS)

        // perform validation
        val fromInstitution = lazyInstitution.value
        validateRelease(custody, fromInstitution, releaseDate, movementReasonCode)

        // create the release
        releaseType?.also {
            release(releaseDateTime, it, custody, fromInstitution, movementReasonCode)
        }

        val statusCode = when {
            releaseType?.code == ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE.code -> CUSTODY_ROTL
            movementReasonCode.isHospitalRelease() -> {
                if (custody.isInCustody()) {
                    IN_CUSTODY
                } else if (custody.status.code == RELEASED_ON_LICENCE.code) {
                    RECALLED
                } else {
                    throw IgnorableMessageException(
                        "NoActionHospitalRelease",
                        listOfNotNull(
                            "currentStatusCode" to custody.status.code,
                            "custodyStatusDescription" to custody.status.description,
                            custody.institution?.code?.let { "currentLocation" to it }
                        ).toMap()
                    )
                }
            }

            else -> RELEASED_ON_LICENCE
        }
        custodyService.updateStatus(
            custody,
            statusCode,
            releaseDate,
            when {
                statusCode == CUSTODY_ROTL -> "Released on Temporary Licence"
                movementReasonCode.isHospitalRelease() -> "Transfer to/from Hospital"
                else -> "Released on Licence"
            }
        )

        val institution = when (releaseType?.code) {
            ReleaseTypeCode.ADULT_LICENCE.code -> institutionRepository.getByCode(InstitutionCode.IN_COMMUNITY.code)
            else -> fromInstitution
        }
        if (locationChanged(custody, institution)) {
            custodyService.updateLocation(custody, institution, releaseDate)
            if (!movementReasonCode.isHospitalRelease()) {
                custodyService.allocatePrisonManager(institution, custody, releaseDateTime)
            }
        }

        if (statusCode == RECALLED) {
            val recallReason =
                recallReasonRepository.getByCodeAndSelectableIsTrue(RecallReasonCode.TRANSFER_TO_SECURE_HOSPITAL.code)
            recallService.createRecall(custody, recallReason, releaseDateTime, custody.mostRecentRelease())
        }
    }

    private fun locationChanged(custody: Custody, institution: Institution): Boolean =
        institution.code == InstitutionCode.IN_COMMUNITY.code || custody.institution?.id != institution.id

    private fun validateRelease(
        custody: Custody,
        institution: Institution,
        releaseDate: ZonedDateTime,
        movementReasonCode: String
    ) {
        // do not carry out this validation for ETL23 - logic later to deal with these scenarios
        if (movementReasonCode != MovementReasonCodes.EXTENDED_TEMPORARY_LICENCE && !movementReasonCode.isHospitalRelease()) {
            if (!custody.isInCustody()) {
                throw IgnorableMessageException("UnexpectedCustodialStatus")
            }

            // This behaviour may change. See https://dsdmoj.atlassian.net/browse/PI-264
            if (custody.institution?.code != institution.code) {
                throw IgnorableMessageException(
                    "UnexpectedInstitution",
                    mapOf("current" to (custody.institution?.code ?: "null"))
                )
            }
        }

        if (releaseDate.isBefore(custody.disposal.date)) {
            throw IgnorableMessageException("InvalidReleaseDate")
        }

        val previousRecallDate = custody.mostRecentRelease()?.recall?.date
        if (previousRecallDate != null && releaseDate.isBefore(previousRecallDate)) {
            throw IgnorableMessageException("InvalidReleaseDate")
        }
    }

    private fun mapToReleaseType(reason: String, movementReasonCode: String): ReleaseTypeCode? = when (reason) {
        "RELEASED" -> {
            if (movementReasonCode.isHospitalRelease() && featureFlags.enabled(HOSPITAL_RELEASE)) {
                null
            } else if (movementReasonCode.isHospitalRelease()) {
                throw IgnorableMessageException("UnsupportedReleaseReason")
            } else {
                ReleaseTypeCode.ADULT_LICENCE
            }
        }

        "TEMPORARY_ABSENCE_RELEASE" ->
            if (movementReasonCode == "ETL23" && featureFlags.enabled(RELEASE_ETL23)) {
                ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE
            } else {
                throw IgnorableMessageException("UnsupportedReleaseReason")
            }

        "RELEASED_TO_HOSPITAL" -> {
            if (movementReasonCode.isHospitalRelease() && featureFlags.enabled(HOSPITAL_RELEASE)) {
                null
            } else {
                throw IgnorableMessageException("UnsupportedReleaseReason")
            }
        }

        "SENT_TO_COURT",
        "TRANSFERRED" -> throw IgnorableMessageException("UnsupportedReleaseReason")

        else -> throw IllegalArgumentException("Unexpected release reason: $reason")
    }

    private fun release(
        dateTime: ZonedDateTime,
        type: ReferenceData,
        custody: Custody,
        fromInstitution: Institution,
        movementReasonCode: String
    ) {
        if (!custody.status.canRelease()) return
        val length = if (type.code == ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE.code) {
            val acr = custodyService.findAutoConditionalReleaseDate(custody.id)
                ?: throw IgnorableMessageException("No Auto-Conditional Release date is present")
            if (acr.date.isBefore(now())) throw IgnorableMessageException("Auto-Conditional Release date in the past")
            custodyService.addRotlEndDate(acr)
            maxOf(DAYS.between(acr.date.minusDays(1), now()), 1)
        } else {
            null
        }
        val releaseDate = dateTime.truncatedTo(DAYS)
        releaseRepository.save(
            Release(
                date = releaseDate,
                type = type,
                person = custody.disposal.event.person,
                custody = custody,
                institutionId = fromInstitution.id,
                probationAreaId = hostRepository.findLeadHostProviderIdByInstitutionId(
                    fromInstitution.id.institutionId,
                    dateTime
                ),
                recall = custody.mostRecentRelease()?.recall,
                length = length
            )
        )
        val event = custody.disposal.event
        val orderManager = orderManagerRepository.getByEventId(event.id)
        contactRepository.save(
            Contact(
                type = contactTypeRepository.getByCode(ContactTypeCode.RELEASE_FROM_CUSTODY.code),
                date = dateTime,
                event = event,
                person = event.person,
                notes = if (movementReasonCode == MovementReasonCodes.EXTENDED_TEMPORARY_LICENCE) ETL23_NOTES else "Release Type: ${type.description}",
                staffId = orderManager.staffId,
                teamId = orderManager.teamId
            )
        )
        eventService.updateReleaseDateAndIapsFlag(event, releaseDate)
    }
}

fun ReferenceData.canRelease() = RELEASABLE_STATUSES.map { it.code }.contains(code)

fun String.isHospitalRelease() =
    this in listOf(
        MovementReasonCodes.DETAINED_MENTAL_HEALTH,
        MovementReasonCodes.RELEASE_MENTAL_HEALTH,
        MovementReasonCodes.FINAL_DISCHARGE_PSYCHIATRIC
    )
