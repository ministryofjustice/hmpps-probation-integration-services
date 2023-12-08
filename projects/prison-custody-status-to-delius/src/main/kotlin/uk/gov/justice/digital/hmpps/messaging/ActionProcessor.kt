package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService

@Service
class ActionProcessor(actionsList: List<PrisonerMovementAction>, private val eventService: EventService) {
    private val actions = actionsList.associateBy { it.name }

    @Transactional(noRollbackFor = [IgnorableMessageException::class])
    fun processActions(
        prisonerMovement: PrisonerMovement,
        actionNames: List<String>,
    ): List<ActionResult> =
        try {
            eventService.getActiveCustodialEvents(prisonerMovement.nomsId)
                .flatMap { event ->
                    actionNames.map {
                        try {
                            actions[it]?.accept(PrisonerMovementContext(prisonerMovement, event.disposal!!.custody!!))
                                ?: throw IllegalArgumentException("Action Not Found: $it")
                        } catch (ie: IgnorableMessageException) {
                            ActionResult.Ignored(
                                ie.message,
                                prisonerMovement.telemetryProperties() + ie.additionalProperties,
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            listOf(ActionResult.Failure(e, prisonerMovement.telemetryProperties()))
        }
}
