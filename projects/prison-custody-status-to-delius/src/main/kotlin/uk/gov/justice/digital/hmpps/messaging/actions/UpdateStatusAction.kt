package uk.gov.justice.digital.hmpps.messaging.actions

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodialStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodyEventType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.NO_CHANGE_STATUSES
import uk.gov.justice.digital.hmpps.messaging.*

@Component
class UpdateStatusAction(
    private val referenceDataRepository: ReferenceDataRepository,
    private val custodyRepository: CustodyRepository,
    private val custodyHistoryRepository: CustodyHistoryRepository
) : PrisonerMovementAction {
    override val name: String = "UpdateStatus"
    override fun accept(context: PrisonerMovementContext): ActionResult {
        val (prisonerMovement, custody) = context

        val result = checkPreconditions(prisonerMovement, custody)
        if (result != null) return result

        return when (context.prisonerMovement) {
            is PrisonerMovement.Received -> inboundStatusChange(context)
            is PrisonerMovement.Released -> outboundStatusChange(context)
        }
    }

    private fun inboundStatusChange(context: PrisonerMovementContext): ActionResult {
        val (prisonerMovement, custody) = context
        val detail = if (custody.canBeRecalled()) "Recall added in custody " else "In custody "
        return updateStatus(custody, CustodialStatusCode.IN_CUSTODY, prisonerMovement, detail)
    }

    private fun outboundStatusChange(context: PrisonerMovementContext): ActionResult {
        val (prisonerMovement, custody) = context
        val statusCode = when {
            prisonerMovement.isHospitalRelease() || prisonerMovement.isIrcRelease() || prisonerMovement.isAbsconded() -> custody.nextStatus()
            else -> CustodialStatusCode.RELEASED_ON_LICENCE
        }
        val detail = when {
            prisonerMovement.isHospitalRelease() -> "Transfer to/from Hospital"
            prisonerMovement.isIrcRelease() -> "Transfer to Immigration Removal Centre"
            prisonerMovement.isAbsconded() -> "Recall added unlawfully at large "
            else -> "Released on Licence"
        }
        return updateStatus(custody, statusCode, prisonerMovement, detail)
    }

    private fun Custody.nextStatus() = when {
        canBeRecalled() -> CustodialStatusCode.RECALLED
        status.canChange() -> CustodialStatusCode.IN_CUSTODY
        else -> throw IgnorableMessageException("PrisonerStatusCorrect")
    }

    private fun updateStatus(
        custody: Custody,
        status: CustodialStatusCode,
        prisonerMovement: PrisonerMovement,
        detail: String
    ): ActionResult = if (status.code == custody.status.code) {
        ActionResult.Ignored("PrisonerStatusCorrect", prisonerMovement.telemetryProperties())
    } else {
        val history = custody.updateStatusAt(
            referenceDataRepository.getCustodialStatus(status.code),
            prisonerMovement.occurredAt,
            detail
        ) { referenceDataRepository.getCustodyEventType(CustodyEventTypeCode.STATUS_CHANGE.code) }
        custodyRepository.save(custody)
        custodyHistoryRepository.save(history)
        ActionResult.Success(ActionResult.Type.StatusUpdated, prisonerMovement.telemetryProperties())
    }

    private fun checkPreconditions(movement: PrisonerMovement, custody: Custody) = if (
        (movement.isReceived() && !(custody.status.canChange() && movement.receivedDateValid(custody))) ||
        (movement.isReleased() && !(custody.canBeReleased() && movement.releaseDateValid(custody))) ||
        (movement.isToSecureUnitOutsidePrison() && !movement.occurredAfter(custody.statusChangeDate))
    ) ActionResult.Ignored("PrisonerStatusCorrect", movement.telemetryProperties()) else null
}

private fun ReferenceData.canChange() = !NO_CHANGE_STATUSES.map { it.code }.contains(code)
