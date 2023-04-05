package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getReleaseType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseOutcome.PrisonerDied
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseOutcome.PrisonerReleased
import java.time.LocalDate.now
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS

const val ETL23_NOTES = "This is a ROTL release on Extended Temporary Licence (ETL23)"

enum class ReleaseOutcome {
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
    private val featureFlags: FeatureFlags
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun releaseFrom(
        nomsNumber: String,
        prisonId: String,
        reason: String,
        movementReasonCode: String,
        releaseDateTime: ZonedDateTime
    ) = if (movementReasonCode == MovementReasonCodes.DIED) {
        personDied.inCustody(nomsNumber, releaseDateTime)
        PrisonerDied
    } else {
        val releaseType = referenceDataRepository.getReleaseType(mapToReleaseType(reason, movementReasonCode).code)
        val institution = institutionRepository.getByNomisCdeCode(prisonId)

        eventService.getActiveCustodialEvents(nomsNumber).forEach {
            addReleaseToEvent(
                it,
                institution,
                releaseType,
                releaseDateTime,
                movementReasonCode
            )
        }
        PrisonerReleased
    }

    private fun addReleaseToEvent(
        event: Event,
        fromInstitution: Institution,
        releaseType: ReferenceData,
        releaseDateTime: ZonedDateTime,
        movementReasonCode: String
    ): Unit = audit(BusinessInteractionCode.ADD_RELEASE) {
        it["eventId"] = event.id
        OptimisationContext.offenderId.set(event.person.id)

        val disposal = event.disposal ?: throw NotFoundException("Disposal", "eventId", event.id)
        val custody = disposal.custody ?: throw NotFoundException("Custody", "disposalId", disposal.id)
        val releaseDate = releaseDateTime.truncatedTo(DAYS)

        // perform validation
        validateRelease(custody, fromInstitution, releaseDate, movementReasonCode)

        // create the release
        releaseFrom(releaseDateTime, releaseType, custody, fromInstitution, movementReasonCode)

        // update custody status + location
        val statusCode = when (releaseType.code) {
            ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE.code -> CustodialStatusCode.CUSTODY_ROTL
            else -> CustodialStatusCode.RELEASED_ON_LICENCE
        }
        custodyService.updateStatus(
            custody,
            statusCode,
            releaseDate,
            when (statusCode) {
                CustodialStatusCode.CUSTODY_ROTL -> "Released on Temporary Licence"
                else -> "Released on Licence"
            }
        )

        val institution = when (releaseType.code) {
            ReleaseTypeCode.ADULT_LICENCE.code -> institutionRepository.getByCode(InstitutionCode.IN_COMMUNITY.code)
            else -> fromInstitution
        }
        if (locationChanged(custody, institution)) {
            custodyService.updateLocation(custody, institution, releaseDate)
        }

        custodyService.allocatePrisonManager(null, fromInstitution, custody, releaseDateTime)

        // update event
        eventService.updateReleaseDateAndIapsFlag(event, releaseDate)
    }

    private fun locationChanged(custody: Custody, institution: Institution): Boolean =
        institution.code == InstitutionCode.IN_COMMUNITY.code || custody.institution.id != institution.id

    private fun validateRelease(custody: Custody, institution: Institution, releaseDate: ZonedDateTime, movementReasonCode: String) {
        if (!custody.isInCustody()) {
            throw IgnorableMessageException("UnexpectedCustodialStatus")
        }

        // This behaviour may change. See https://dsdmoj.atlassian.net/browse/PI-264
        if (custody.institution.code != institution.code && movementReasonCode != MovementReasonCodes.EXTENDED_TEMPORARY_LICENCE) {
            throw IgnorableMessageException("UnexpectedInstitution", mapOf("current" to custody.institution.code))
        }

        if (releaseDate.isBefore(custody.disposal.date)) {
            throw IgnorableMessageException("InvalidReleaseDate")
        }

        val previousRecallDate = custody.mostRecentRelease()?.recall?.date
        if (previousRecallDate != null && releaseDate.isBefore(previousRecallDate)) {
            throw IgnorableMessageException("InvalidReleaseDate")
        }
    }

    private fun mapToReleaseType(reason: String, movementReasonCode: String) = when (reason) {
        "RELEASED" -> ReleaseTypeCode.ADULT_LICENCE
        "TEMPORARY_ABSENCE_RELEASE" ->
            if (movementReasonCode == "ETL23" && featureFlags.enabled(RELEASE_ETL23)) {
                ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE
            } else {
                throw IgnorableMessageException("UnsupportedReleaseReason")
            }
        "RELEASED_TO_HOSPITAL", // -> TBC
        "SENT_TO_COURT",
        "TRANSFERRED" -> throw IgnorableMessageException("UnsupportedReleaseReason")
        else -> throw IllegalArgumentException("Unexpected release reason: $reason")
    }

    private fun releaseFrom(
        dateTime: ZonedDateTime,
        type: ReferenceData,
        custody: Custody,
        fromInstitution: Institution,
        movementReasonCode: String
    ) {
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
    }
}
