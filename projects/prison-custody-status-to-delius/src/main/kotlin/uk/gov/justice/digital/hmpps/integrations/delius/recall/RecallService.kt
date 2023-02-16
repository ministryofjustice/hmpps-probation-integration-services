package uk.gov.justice.digital.hmpps.integrations.delius.recall

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datasource.OptimisationContext
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.custody.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.CustodyService
import uk.gov.justice.digital.hmpps.integrations.delius.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService
import uk.gov.justice.digital.hmpps.integrations.delius.event.manager.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.manager.getByEventId
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.LicenceConditionService
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.PrisonManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.getByNomisCdeCodeAndIdEstablishment
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.getByCodeAndSelectableIsTrue
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.isEotl
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.NO_CHANGE_STATUSES
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.NO_RECALL_STATUSES
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.TERMINATED_STATUSES
import uk.gov.justice.digital.hmpps.integrations.delius.release.Release
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS

enum class RecallOutcome {
    PrisonerRecalled,
    CustodialDetailsUpdated,
    NoCustodialUpdates
}

val EOTL_RECALL_CONTACT_NOTES = """${System.lineSeparator()}
    |The date of the change to this Recall/Return to Custody has been identified from the case being updated following a Temporary Absence Return in NOMIS.
    |The date may reflect an update after the date the actual Recall/Return to Custody occurred
""".trimMargin()

@Service
class RecallService(
    auditedInteractionService: AuditedInteractionService,
    private val eventService: EventService,
    private val institutionRepository: InstitutionRepository,
    private val recallReasonRepository: RecallReasonRepository,
    private val recallRepository: RecallRepository,
    private val custodyService: CustodyService,
    private val licenceConditionService: LicenceConditionService,
    private val orderManagerRepository: OrderManagerRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val contactAlertRepository: ContactAlertRepository,
    private val prisonManagerService: PrisonManagerService
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun recall(
        nomsNumber: String,
        prisonId: String,
        reason: String,
        recallDateTime: ZonedDateTime
    ): RecallOutcome {
        val recallReason = recallReasonRepository.getByCodeAndSelectableIsTrue(mapToRecallReason(reason).code)
        val institution = institutionRepository.getByNomisCdeCodeAndIdEstablishment(prisonId)

        return eventService.getActiveCustodialEvents(nomsNumber)
            .map { addRecallToEvent(it, institution, recallReason, recallDateTime) }
            .minBy { it.ordinal } // return the most relevant outcome
    }

    private fun addRecallToEvent(
        event: Event,
        toInstitution: Institution,
        recallReason: RecallReason,
        recallDateTime: ZonedDateTime
    ): RecallOutcome = audit(BusinessInteractionCode.ADD_RECALL) { audit ->
        audit["eventId"] = event.id
        OptimisationContext.offenderId.set(event.person.id)

        val person = event.person
        val disposal = event.disposal ?: throw NotFoundException("Disposal", "eventId", event.id)
        val custody = disposal.custody ?: throw NotFoundException("Custody", "disposalId", disposal.id)
        val latestRelease = custody.mostRecentRelease()
        if (latestRelease == null && custody.status.canRecall()) {
            throw IgnorableMessageException("MissingRelease")
        }

        val recallDate = recallDateTime.truncatedTo(DAYS)

        // perform validation
        validateRecall(recallReason, custody, latestRelease, recallDate)

        val recall = createRecall(custody, recallReason, recallDate, latestRelease, person)

        val custodialStatusUpdated = updateCustodialStatus(toInstitution, custody, recallDate, recall)

        val custodialLocationUpdated = updateCustodialLocation(custody, toInstitution, event, recallDate, recallReason)

        allocatePrisonManager(latestRelease, toInstitution, custody, disposal, recallDateTime)

        if (recall != null) {
            licenceConditionService.terminateLicenceConditionsForDisposal(
                disposalId = disposal.id,
                terminationReason = recallReason.licenceConditionTerminationReason,
                terminationDate = recallDate,
                endOfTemporaryLicence = recallReason.isEotl()
            )
            createRecallAlertContact(event, person, recallDateTime, recallReason, recall)
            RecallOutcome.PrisonerRecalled
        } else if (custodialStatusUpdated || custodialLocationUpdated) {
            RecallOutcome.CustodialDetailsUpdated
        } else {
            RecallOutcome.NoCustodialUpdates
        }
    }

    private fun updateCustodialLocation(
        custody: Custody,
        toInstitution: Institution,
        event: Event,
        recallDate: ZonedDateTime,
        recallReason: RecallReason
    ) = if (custody.institution.id != toInstitution.id || custody.status.canRecall()) {
        val orderManager = orderManagerRepository.getByEventId(event.id)
        custodyService.updateLocation(custody, toInstitution.code, recallDate, orderManager, recallReason)
        true
    } else {
        false
    }

    private fun updateCustodialStatus(
        toInstitution: Institution,
        custody: Custody,
        recallDate: ZonedDateTime,
        recall: Recall?
    ) = when (toInstitution.code) {
        InstitutionCode.UNLAWFULLY_AT_LARGE.code -> {
            custodyService.updateStatus(custody, CustodialStatusCode.RECALLED, recallDate, "Recall added unlawfully at large ")
            true
        }
        InstitutionCode.UNKNOWN.code -> {
            custodyService.updateStatus(custody, CustodialStatusCode.RECALLED, recallDate, "Recall added but location unknown ")
            true
        }
        else -> if (custody.status.canChange()) {
            val detail = if (recall == null) "In custody " else "Recall added in custody "
            custodyService.updateStatus(custody, CustodialStatusCode.IN_CUSTODY, recallDate, detail)
            true
        } else {
            false
        }
    }

    private fun createRecallAlertContact(
        event: Event,
        person: Person,
        recallDateTime: ZonedDateTime,
        recallReason: RecallReason,
        recall: Recall
    ) {
        val orderManager = orderManagerRepository.getByEventId(event.id)
        val personManager = personManagerRepository.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id)
        val notes = "Reason for Recall: ${recallReason.description}" +
            if (recallReason.isEotl()) EOTL_RECALL_CONTACT_NOTES else ""
        val contact = contactRepository.save(
            Contact(
                type = contactTypeRepository.getByCode(ContactTypeCode.BREACH_PRISON_RECALL.code),
                date = recallDateTime,
                event = event,
                person = person,
                notes = notes,
                staffId = orderManager.staffId,
                teamId = orderManager.teamId,
                createdDatetime = recall.createdDatetime,
                alert = true
            )
        )
        contactAlertRepository.save(
            ContactAlert(
                contactId = contact.id,
                typeId = contact.type.id,
                personId = person.id,
                personManagerId = personManager.id,
                staffId = personManager.staff.id,
                teamId = personManager.team.id
            )
        )
    }

    private fun allocatePrisonManager(
        latestRelease: Release?,
        toInstitution: Institution,
        custody: Custody,
        disposal: Disposal,
        recallDateTime: ZonedDateTime
    ) {
        // allocate a prison manager if institution has changed and institution is linked to a provider
        if ((
            (latestRelease != null && toInstitution.id != latestRelease.institutionId) ||
                (latestRelease == null && toInstitution.id != custody.institution.id)
            ) &&
            toInstitution.probationArea != null
        ) {
            prisonManagerService.allocateToProbationArea(disposal, toInstitution.probationArea, recallDateTime)
        }
    }

    private fun createRecall(
        custody: Custody,
        recallReason: RecallReason,
        recallDate: ZonedDateTime,
        latestRelease: Release?,
        person: Person
    ): Recall? {
        return if (!custody.status.canRecall()) {
            null
        } else {
            recallRepository.save(
                Recall(
                    date = recallDate,
                    reason = recallReason,
                    release = latestRelease!!, // Only possible to be null if no recall to be created
                    person = person
                )
            )
        }
    }

    private fun validateRecall(
        reason: RecallReason,
        custody: Custody,
        latestRelease: Release?,
        recallDate: ZonedDateTime
    ) {
        if (custody.status.code == CustodialStatusCode.POST_SENTENCE_SUPERVISION.code ||
            (reason.code == RecallReasonCode.END_OF_TEMPORARY_LICENCE.code && custody.status.code == CustodialStatusCode.IN_CUSTODY_IRC.code)
        ) {
            throw IgnorableMessageException("UnexpectedCustodialStatus")
        }

        if (reason.code == RecallReasonCode.END_OF_TEMPORARY_LICENCE.code && custody.status.code == CustodialStatusCode.RELEASED_ON_LICENCE.code) {
            throw ConflictException("Recall from Temporary Licence, however Released on Licence")
        }

        if (custody.status.isTerminated()) {
            throw IllegalArgumentException("TerminatedCustodialStatus")
        }

        val recall = latestRelease?.recall
        if (recall != null && reason.code != RecallReasonCode.END_OF_TEMPORARY_LICENCE.code) {
            throw IgnorableMessageException("RecallAlreadyExists")
        }

        if (latestRelease?.type?.code != ReleaseTypeCode.ADULT_LICENCE.code &&
            reason.code != RecallReasonCode.END_OF_TEMPORARY_LICENCE.code
        ) {
            throw IgnorableMessageException("UnexpectedReleaseType")
        }

        if (recallDate.isAfter(ZonedDateTime.now()) ||
            (latestRelease != null && recallDate.isBefore(latestRelease.date))
        ) {
            throw IgnorableMessageException("InvalidRecallDate")
        }
    }

    private fun mapToRecallReason(reason: String) = when (reason) {
        "ADMISSION" -> RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT
        "TEMPORARY_ABSENCE_RETURN" -> RecallReasonCode.END_OF_TEMPORARY_LICENCE
        "RETURN_FROM_COURT",
        "TRANSFERRED",
        "UNKNOWN" -> throw IgnorableMessageException("UnsupportedRecallReason")
        else -> throw IllegalArgumentException("Unexpected recall reason: $reason")
    }

    private fun ReferenceData.canRecall() = !NO_RECALL_STATUSES.map { it.code }.contains(code)
    private fun ReferenceData.canChange() = !NO_CHANGE_STATUSES.map { it.code }.contains(code)

    private fun ReferenceData.isTerminated() = TERMINATED_STATUSES.map { it.code }.contains(code)
}
