package uk.gov.justice.digital.hmpps.messaging.actions

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactDetail
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.canBeRecalled
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.LicenceConditionService
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReason
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.RecallRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.isEotl
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode.Companion.withCode
import uk.gov.justice.digital.hmpps.messaging.ActionResult
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementAction
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementContext
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class RecallAction(
    private val recallReasonRepository: RecallReasonRepository,
    private val recallRepository: RecallRepository,
    private val licenceConditionService: LicenceConditionService,
    private val contactService: ContactService
) : PrisonerMovementAction {

    private val eotlRecallContactNotes = """${System.lineSeparator()}
    |The date of the change to this Recall/Return to Custody has been identified from the case being updated following a Temporary Absence Return in NOMIS.
    |The date may reflect an update after the date the actual Recall/Return to Custody occurred
    """.trimMargin()

    override val name: String
        get() = "Recall"

    override fun accept(context: PrisonerMovementContext): ActionResult {
        val (prisonerMovement, custody) = context
        checkPreconditions(prisonerMovement, custody)
        val recallReason = recallReasonRepository.getByCode(
            recallReason(
                prisonerMovement,
                withCode(custody.status.code)
            ).value
        )
        val person = custody.disposal.event.person
        val recall = recallRepository.save(
            Recall(
                date = prisonerMovement.occurredAt.truncatedTo(ChronoUnit.DAYS),
                reason = recallReason,
                release = custody.mostRecentRelease()!!,
                person = person
            )
        )

        val notes = "Reason for Recall: ${recallReason.description}" +
            if (recallReason.isEotl()) eotlRecallContactNotes else ""
        contactService.createContact(
            ContactDetail(
                ContactType.Code.BREACH_PRISON_RECALL,
                prisonerMovement.occurredAt,
                notes,
                true,
                recall.createdDatetime
            ),
            custody.disposal.event.person,
            custody.disposal.event,
            custody.disposal.event.manager()
        )
        if (prisonerMovement is PrisonerMovement.Received) {
            licenceConditionService.terminateLicenceConditionsForDisposal(
                disposalId = custody.disposal.id,
                terminationReason = recallReason.licenceConditionTerminationReason,
                terminationDate = recall.date,
                endOfTemporaryLicence = recallReason.isEotl()
            )
        }
        return ActionResult.Success(ActionResult.Type.Recalled, prisonerMovement.telemetryProperties())
    }

    private fun checkPreconditions(prisonerMovement: PrisonerMovement, custody: Custody) {
        val latestRelease = custody.mostRecentRelease()
        if (!custody.canBeRecalled()) {
            throw IgnorableMessageException("RecallNotRequired")
        }

        if (prisonerMovement.occurredAt.isAfter(ZonedDateTime.now()) ||
            (prisonerMovement.occurredAt.isBefore(latestRelease?.date))
        ) {
            throw IgnorableMessageException("InvalidRecallDate")
        }
    }

    private fun recallReason(
        prisonerMovement: PrisonerMovement,
        statusCode: CustodialStatusCode
    ): RecallReason.Code = when (prisonerMovement.type) {
        PrisonerMovement.Type.ADMISSION -> {
            RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT
        }

        PrisonerMovement.Type.TEMPORARY_ABSENCE_RETURN -> {
            RecallReason.Code.END_OF_TEMPORARY_LICENCE
        }

        PrisonerMovement.Type.TRANSFERRED -> {
            when (statusCode) {
                CustodialStatusCode.CUSTODY_ROTL -> RecallReason.Code.END_OF_TEMPORARY_LICENCE
                else -> RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT
            }
        }

        PrisonerMovement.Type.RELEASED -> when {
            prisonerMovement.isAbsconded() -> RecallReason.Code.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT
            prisonerMovement.isHospitalRelease() -> RecallReason.Code.TRANSFER_TO_SECURE_HOSPITAL
            prisonerMovement.isIrcRelease() -> RecallReason.Code.TRANSFER_TO_IRC
            else -> throw IgnorableMessageException("RecallNotSupported", prisonerMovement.telemetryProperties())
        }

        else -> throw IgnorableMessageException("RecallNotSupported", prisonerMovement.telemetryProperties())
    }
}
