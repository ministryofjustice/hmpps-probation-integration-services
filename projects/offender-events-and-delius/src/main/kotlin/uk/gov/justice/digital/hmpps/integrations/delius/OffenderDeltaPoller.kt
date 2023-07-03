package uk.gov.justice.digital.hmpps.integrations.delius

import io.sentry.Sentry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class OffenderDeltaPoller(private val service: OffenderDeltaService, private val telemetryService: TelemetryService) {

    @Scheduled(fixedDelayString = "\${offender-events.fixed-delay:100}")
    fun checkAndSendEvents() {
        try {
            val counts = service.checkAndSendEvents()
            if (counts.first > 0 || counts.second > 0) {
                telemetryService.trackEvent(
                    "OffenderEventsProcessed",
                    mapOf("EventsSent" to counts.first.toString(), "PersonNotFound" to counts.second.toString())
                )
            }
        } catch (e: Exception) {
            telemetryService.trackEvent(
                "OffenderEventsProcessingFailed",
                mapOf("Exception" to (e.message ?: "Unknown Exception"))
            )
            Sentry.captureException(e)
        }
    }
}
