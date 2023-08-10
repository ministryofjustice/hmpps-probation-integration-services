package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.FeatureFlagCodes.HOSPITAL_RELEASE
import uk.gov.justice.digital.hmpps.MovementReasonCodes
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datasource.OptimisationContext
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactDetail
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.CustodyService
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.getByEventId
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.host.entity.HostRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.getByNomisCdeCode
import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallService
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getReleaseType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.IN_CUSTODY
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.RECALLED
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.RELEASED_ON_LICENCE
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.RELEASABLE_STATUSES
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseOutcome.MultipleEventsReleased
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseOutcome.PrisonerDied
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseOutcome.PrisonerReleased
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.Release
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.ReleaseRepository
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS

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
    private val contactService: ContactService,
    private val personDied: PersonDied,
    private val recallReasonRepository: RecallReasonRepository,
    private val recallService: RecallService,
    private val featureFlags: FeatureFlags
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun release(release: PrisonerMovement) = if (release.movementReason == MovementReasonCodes.DIED) {
        personDied.inCustody(release.nomsId, release.occurredAt)
        PrisonerDied
    } else {
        val releaseType = mapToReleaseType(release.reason, release.movementReason)?.let {
            referenceDataRepository.getReleaseType(it.code)
        }
        val institution = lazy {
            release.prisonId?.let { institutionRepository.getByNomisCdeCode(it) }
                ?: institutionRepository.getByCode(InstitutionCode.OTHER_SECURE_UNIT.code)
        }

        val events = eventService.getActiveCustodialEvents(release.nomsId)
        events.forEach {
            addReleaseToEvent(
                it,
                institution,
                releaseType,
                release.occurredAt,
                release.movementReason
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
            release(releaseDateTime, it, custody, fromInstitution)
        }

        val statusCode = when {
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
                movementReasonCode.isHospitalRelease() -> "Transfer to/from Hospital"
                else -> "Released on Licence"
            }
        )

        val institution = when (releaseType?.code) {
            ReleaseTypeCode.ADULT_LICENCE.code -> institutionRepository.getByCode(InstitutionCode.IN_COMMUNITY.code)
            else -> fromInstitution
        }

        if (custody.institution?.id != institution.id) {
            custodyService.updateLocation(custody, institution, releaseDate)
        }

        if (statusCode == RECALLED) {
            val recallReason =
                recallReasonRepository.getByCode(RecallReason.Code.TRANSFER_TO_SECURE_HOSPITAL.value)
            recallService.createRecall(custody, recallReason, releaseDateTime, custody.mostRecentRelease())
        }
    }

    private fun validateRelease(
        custody: Custody,
        institution: Institution,
        releaseDate: ZonedDateTime,
        movementReasonCode: String
    ) {
        if (!movementReasonCode.isHospitalRelease()) {
            if (!custody.isInCustody()) {
                throw IgnorableMessageException("UnexpectedCustodialStatus")
            }

            // This behaviour may change. See https://dsdmoj.atlassian.net/browse/PI-264
            // If we remove this - we need to handle PrisonerMovement.Released from identifier added/updated
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

        "TEMPORARY_ABSENCE_RELEASE" -> throw IgnorableMessageException("UnsupportedReleaseReason")
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
        fromInstitution: Institution
    ) {
        if (!custody.status.canRelease()) return
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
                recall = custody.mostRecentRelease()?.recall
            )
        )
        val event = custody.disposal.event
        val orderManager = orderManagerRepository.getByEventId(event.id)
        contactService.createContact(
            ContactDetail(
                ContactType.Code.RELEASE_FROM_CUSTODY,
                dateTime,
                "Release Type: ${type.description}"
            ),
            event.person,
            event,
            orderManager
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
