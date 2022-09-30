package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datasource.OptimisationContext
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
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
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.getByNomisCdeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getReleaseType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import java.time.ZonedDateTime

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
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun release(
        nomsNumber: String,
        prisonId: String,
        reason: String,
        releaseDate: ZonedDateTime,
    ) {
        val releaseType = referenceDataRepository.getReleaseType(mapToReleaseType(reason).code)
        val institution = institutionRepository.getByNomisCdeCode(prisonId)

        eventService.getActiveCustodialEvents(nomsNumber).forEach {
            addReleaseToEvent(it, institution, releaseType, releaseDate)
        }
    }

    private fun addReleaseToEvent(
        event: Event,
        fromInstitution: Institution,
        releaseType: ReferenceData,
        releaseDate: ZonedDateTime
    ) = audit(BusinessInteractionCode.ADD_RELEASE) {
        it["eventId"] = event.id
        OptimisationContext.offenderId.set(event.person.id)

        val disposal = event.disposal ?: throw NotFoundException("Disposal", "eventId", event.id)
        val custody = disposal.custody ?: throw NotFoundException("Custody", "disposalId", disposal.id)

        // perform validation
        validateRelease(custody, fromInstitution, releaseDate)

        // create the release
        releaseRepository.save(
            Release(
                date = releaseDate,
                type = releaseType,
                person = event.person,
                custody = custody,
                institutionId = fromInstitution.id,
                probationAreaId = hostRepository
                    .findLeadHostProviderIdByInstitutionId(fromInstitution.id.institutionId, releaseDate),
                recall = custody.mostRecentRelease()?.recall,
            )
        )

        // update custody status + location
        custodyService.updateStatus(custody, CustodialStatusCode.RELEASED_ON_LICENCE, releaseDate, "Released on Licence")
        custodyService.updateLocation(custody, InstitutionCode.IN_COMMUNITY.code, releaseDate)

        // update event
        eventService.updateReleaseDateAndIapsFlag(event, releaseDate)

        // create contact
        val orderManager = orderManagerRepository.getByEventId(event.id)
        contactRepository.save(
            Contact(
                type = contactTypeRepository.getByCode(ContactTypeCode.RELEASE_FROM_CUSTODY.code),
                date = releaseDate,
                event = event,
                person = event.person,
                notes = "Release Type: ${releaseType.description}",
                staffId = orderManager.staffId,
                teamId = orderManager.teamId,
            )
        )
    }

    private fun validateRelease(custody: Custody, institution: Institution, releaseDate: ZonedDateTime) {
        if (!custody.isInCustody()) {
            throw IgnorableMessageException("UnexpectedCustodialStatus")
        }

        // This behaviour may change. See https://dsdmoj.atlassian.net/browse/PI-264
        if (custody.institution.code != institution.code) {
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

    private fun mapToReleaseType(reason: String) = when (reason) {
        "RELEASED" -> ReleaseTypeCode.ADULT_LICENCE
        "TEMPORARY_ABSENCE_RELEASE", // -> ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE
        "RELEASED_TO_HOSPITAL", // -> TBC
        "SENT_TO_COURT",
        "TRANSFERRED" -> throw IgnorableMessageException("UnsupportedReleaseReason")
        else -> throw IllegalArgumentException("Unexpected release reason: $reason")
    }
}
