package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.service.entity.EventRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class GoalsService(
    private val eventRepository: EventRepository,
    private val telemetryService: TelemetryService,
) {

    @Transactional
    fun goalsAchieved(event: HmppsDomainEvent) {
        val crn = event.crn
        val activeEvents = eventRepository.findActiveEventsWithActiveDisposals(crn)

        if (activeEvents.isEmpty()) {
            telemetryService.trackEvent(
                "GoalsAchievedNoActiveDisposal",
                mapOf("crn" to crn, "message" to "[crn=$crn]. No active disposal identified when Goals achieved.")
            )
            return
        }

        activeEvents.forEach { e ->
            e.spGoalsComplete = "Y"
            e.spGoalsDate = event.occurredAt.toLocalDate()
        }
        eventRepository.saveAll(activeEvents)
    }

    @Transactional
    fun goalsAchievedRemoved(event: HmppsDomainEvent) {
        val crn = event.crn
        val activeEvents = eventRepository.findActiveEventsWithActiveDisposals(crn)

        if (activeEvents.isEmpty()) {
            telemetryService.trackEvent(
                "GoalsAchievedRemovedNoActiveDisposal",
                mapOf(
                    "crn" to crn,
                    "message" to "[crn=$crn]. No active disposal identified when Goals achieved is removed."
                )
            )
            return
        }

        activeEvents.forEach { e ->
            e.spGoalsComplete = null
            e.spGoalsDate = null
        }
        eventRepository.saveAll(activeEvents)
    }
}