package uk.gov.justice.digital.hmpps.integrations.delius.recall

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datasource.OptimisationContext
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
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
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.LicenceConditionService
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.getByNomisCdeCodeAndIdEstablishment
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.getByCodeAndSelectableIsTrue
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.isEotl
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.InstitutionCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.NO_CHANGE_STATUSES
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.NO_RECALL_STATUSES
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.TERMINATED_STATUSES
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.Release
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS

enum class RecallOutcome {
    MultipleEventsRecalled,
    PrisonerRecalled,
    MultipleDetailsUpdated,
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
    private val contactService: ContactService
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun recall(receive: PrisonerMovement): RecallOutcome {
        val getRecallReason = { csc: CustodialStatusCode ->
            recallReasonRepository.getByCodeAndSelectableIsTrue(
                decideRecallReason(
                    receive.reason,
                    receive.movementReason
                )(csc).value
            )
        }
        val institution = lazy { institutionRepository.getByNomisCdeCodeAndIdEstablishment(receive.prisonId!!) }

        return eventService.getActiveCustodialEvents(receive.nomsId)
            .map {
                addRecallToEvent(
                    it,
                    institution,
                    getRecallReason,
                    receive.occurredAt
                )
            }
            .combined()
    }

    private fun addRecallToEvent(
        event: Event,
        lazyInstitution: Lazy<Institution>,
        getRecallReason: (csc: CustodialStatusCode) -> RecallReason,
        recallDateTime: ZonedDateTime
    ): RecallOutcome = audit(BusinessInteractionCode.ADD_RECALL) { audit ->
        audit["eventId"] = event.id
        OptimisationContext.offenderId.set(event.person.id)

        val disposal = event.disposal ?: throw NotFoundException("Disposal", "eventId", event.id)
        val custody = disposal.custody ?: throw NotFoundException("Custody", "disposalId", disposal.id)
        val latestRelease = custody.mostRecentRelease()
        if (latestRelease == null && custody.status.canRecall()) {
            throw IgnorableMessageException("MissingRelease")
        }

        val recallDate = recallDateTime.truncatedTo(DAYS)

        val recallReason = getRecallReason(CustodialStatusCode.withCode(custody.status.code))

        // perform validation
        validateRecall(recallReason, custody, latestRelease, recallDate)

        val recall = createRecall(custody, recallReason, recallDateTime, latestRelease)

        val toInstitution = lazyInstitution.value
        val custodialStatusUpdated = updateCustodialStatus(custody, recallDate, recall)

        val custodialLocationUpdated =
            updateCustodialLocation(custody, toInstitution, event, recallDateTime, recallReason)

        if (recall != null) {
            licenceConditionService.terminateLicenceConditionsForDisposal(
                disposalId = disposal.id,
                terminationReason = recallReason.licenceConditionTerminationReason,
                terminationDate = recallDate,
                endOfTemporaryLicence = recallReason.isEotl()
            )
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
        recallDateTime: ZonedDateTime,
        recallReason: RecallReason
    ) = if (custody.institution?.id != toInstitution.id || custody.status.canRecall()) {
        val orderManager = orderManagerRepository.getByEventId(event.id)
        custodyService.allocatePrisonManager(toInstitution, custody, recallDateTime)
        custodyService.updateLocation(
            custody,
            toInstitution,
            recallDateTime.truncatedTo(DAYS),
            orderManager,
            recallReason
        )
        true
    } else {
        false
    }

    private fun updateCustodialStatus(
        custody: Custody,
        recallDate: ZonedDateTime,
        recall: Recall?
    ) = if (custody.status.canChange() || custody.isRecentAndUnknown()) {
        if (recall == null) {
            custodyService.updateStatus(custody, CustodialStatusCode.IN_CUSTODY, recallDate, "In custody ")
            true
        } else {
            custodyService.updateStatus(
                custody,
                CustodialStatusCode.IN_CUSTODY,
                recallDate,
                "Recall added in custody "
            )
            true
        }
    } else {
        false
    }

    private fun createRecallAlertContact(
        event: Event,
        person: Person,
        recallDateTime: ZonedDateTime,
        recallReason: RecallReason,
        createdDateTimeOverride: ZonedDateTime?
    ) {
        val orderManager = orderManagerRepository.getByEventId(event.id)
        val notes = "Reason for Recall: ${recallReason.description}" +
            if (recallReason.isEotl()) EOTL_RECALL_CONTACT_NOTES else ""
        contactService.createContact(
            ContactDetail(
                ContactType.Code.BREACH_PRISON_RECALL,
                recallDateTime,
                notes,
                true,
                createdDateTimeOverride
            ),
            person,
            event,
            orderManager
        )
    }

    fun createRecall(
        custody: Custody,
        recallReason: RecallReason,
        recallDateTime: ZonedDateTime,
        latestRelease: Release?
    ): Recall? = if (latestRelease == null || latestRelease.recall != null || !custody.status.canRecall()) {
        null
    } else {
        val person = custody.disposal.event.person
        val recall = recallRepository.save(
            Recall(
                date = recallDateTime.truncatedTo(DAYS),
                reason = recallReason,
                release = latestRelease,
                person = person
            )
        )
        createRecallAlertContact(custody.disposal.event, person, recallDateTime, recallReason, recall.createdDatetime)
        recall
    }
}

private fun Custody.isRecentAndUnknown() =
    institution?.code == InstitutionCode.UNKNOWN.code && status.code == CustodialStatusCode.SENTENCED_IN_CUSTODY.code

private fun validateRecall(
    reason: RecallReason,
    custody: Custody,
    latestRelease: Release?,
    recallDate: ZonedDateTime
) {
    if (custody.status.code == CustodialStatusCode.POST_SENTENCE_SUPERVISION.code ||
        (reason.code == RecallReason.Code.END_OF_TEMPORARY_LICENCE.value && custody.status.code == CustodialStatusCode.IN_CUSTODY_IRC.code)
    ) {
        throw IgnorableMessageException("UnexpectedCustodialStatus")
    }

    require(!custody.status.isTerminated()) { "TerminatedCustodialStatus" }

    if (recallDate.isAfter(ZonedDateTime.now()) ||
        (latestRelease != null && recallDate.isBefore(latestRelease.date))
    ) {
        throw IgnorableMessageException("InvalidRecallDate")
    }
}

private fun decideRecallReason(
    reason: String,
    movementReason: String
): (code: CustodialStatusCode) -> RecallReason.Code = when (reason) {
    "ADMISSION" -> {
        { RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT }
    }

    "TEMPORARY_ABSENCE_RETURN" -> {
        { RecallReason.Code.END_OF_TEMPORARY_LICENCE }
    }

    "TRANSFERRED" -> {
        if (movementReason == "INT") {
            {
                when (it) {
                    CustodialStatusCode.CUSTODY_ROTL -> RecallReason.Code.END_OF_TEMPORARY_LICENCE
                    else -> RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT
                }
            }
        } else {
            throw IgnorableMessageException("UnsupportedRecallReason")
        }
    }

    "RETURN_FROM_COURT",
    "UNKNOWN" -> throw IgnorableMessageException("UnsupportedRecallReason")

    else -> throw IllegalArgumentException("Unexpected recall reason: $reason")
}

private fun ReferenceData.canRecall() = !NO_RECALL_STATUSES.map { it.code }.contains(code)
private fun ReferenceData.canChange() = !NO_CHANGE_STATUSES.map { it.code }.contains(code)

private fun ReferenceData.isTerminated() = TERMINATED_STATUSES.map { it.code }.contains(code)

fun List<RecallOutcome>.combined(): RecallOutcome {
    if (size == 1) return first()
    return when (val outcome = minBy { it.ordinal }) {
        RecallOutcome.PrisonerRecalled -> RecallOutcome.MultipleEventsRecalled
        RecallOutcome.CustodialDetailsUpdated -> RecallOutcome.MultipleDetailsUpdated
        else -> outcome
    }
}
