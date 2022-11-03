package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class OffenderDeltaPoller(private val service: OffenderDeltaService, private val telemetryService: TelemetryService) {

    @Scheduled(fixedDelayString = "\${offender-events.fixed-delay:100}")
    fun checkAndSendEvents() {
        try {
            val eventCount = service.checkAndSendEvents()
            if (eventCount > 0) {
                telemetryService.trackEvent("OffenderEventsProcessed", mapOf("EventCount" to eventCount.toString()))
            }
        } catch (e: Exception) {
            telemetryService.trackEvent(
                "OffenderEventsProcessingFailed",
                mapOf("Exception" to (e.message ?: "Unknown Exception"))
            )
        }
    }
}
