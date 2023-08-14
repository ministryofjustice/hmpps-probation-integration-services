package uk.gov.justice.digital.hmpps.messaging.actions

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.canRecall
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodialStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodyEventType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.NO_CHANGE_STATUSES
import uk.gov.justice.digital.hmpps.messaging.ActionResult
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovement
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementAction
import uk.gov.justice.digital.hmpps.messaging.PrisonerMovementContext
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties

@Component
class UpdateStatusAction(
    private val referenceDataRepository: ReferenceDataRepository,
    private val custodyRepository: CustodyRepository,
    private val custodyHistoryRepository: CustodyHistoryRepository
) : PrisonerMovementAction {
    override val name: String = "UpdateStatus"
    override fun accept(context: PrisonerMovementContext): ActionResult =
        when (context.prisonerMovement) {
            is PrisonerMovement.Received -> {
                inboundStatusChange(context)
            }

            is PrisonerMovement.Released -> {
                outboundStatusChange(context)
            }
        }

    private fun inboundStatusChange(context: PrisonerMovementContext): ActionResult {
        val (prisonerMovement, custody) = context
        if (custody.status.canChange()) {
            return if (custody.mostRecentRelease() != null && custody.status.canRecall()) {
                updateStatus(
                    custody,
                    CustodialStatusCode.IN_CUSTODY,
                    prisonerMovement,
                    "Recall added in custody "
                )
            } else {
                updateStatus(custody, CustodialStatusCode.IN_CUSTODY, prisonerMovement, "In custody ")
            }
        }
        return ActionResult.Ignored("PrisonerStatusCorrect", prisonerMovement.telemetryProperties())
    }

    private fun outboundStatusChange(context: PrisonerMovementContext): ActionResult {
        val (prisonerMovement, custody) = context
        val statusCode = when {
            prisonerMovement.isHospitalRelease() -> {
                if (custody.isInCustody()) {
                    CustodialStatusCode.IN_CUSTODY
                } else if (custody.status.code == CustodialStatusCode.RELEASED_ON_LICENCE.code) {
                    CustodialStatusCode.RECALLED
                } else {
                    throw IgnorableMessageException(
                        "NoActionHospitalRelease",
                        listOfNotNull(
                            "currentStatusCode" to custody.status.code,
                            "currentStatusDescription" to custody.status.description,
                            custody.institution?.code?.let { "currentLocation" to it }
                        ).toMap()
                    )
                }
            }

            else -> CustodialStatusCode.RELEASED_ON_LICENCE
        }
        return updateStatus(
            custody,
            statusCode,
            prisonerMovement,
            when {
                prisonerMovement.isHospitalRelease() -> "Transfer to/from Hospital"
                else -> "Released on Licence"
            }
        )
    }

    private fun updateStatus(
        custody: Custody,
        status: CustodialStatusCode,
        prisonerMovement: PrisonerMovement,
        detail: String
    ): ActionResult = custody.updateStatusAt(
        referenceDataRepository.getCustodialStatus(status.code),
        prisonerMovement.occurredAt,
        detail
    ) {
        referenceDataRepository.getCustodyEventType(CustodyEventTypeCode.STATUS_CHANGE.code)
    }?.let { history ->
        custodyRepository.save(custody)
        custodyHistoryRepository.save(history)
        return ActionResult.Success(ActionResult.Type.StatusUpdated, prisonerMovement.telemetryProperties())
    } ?: ActionResult.Ignored("PrisonerStatusCorrect")
}

private fun ReferenceData.canChange() = !NO_CHANGE_STATUSES.map { it.code }.contains(code)
