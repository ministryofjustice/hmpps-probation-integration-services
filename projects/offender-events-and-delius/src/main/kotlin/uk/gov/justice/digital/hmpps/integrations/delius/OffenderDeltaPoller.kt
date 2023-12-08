package uk.gov.justice.digital.hmpps.integrations.delius

import io.sentry.Sentry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

@Service
class OffenderDeltaPoller(private val service: OffenderDeltaService, private val telemetryService: TelemetryService) {
    @Scheduled(fixedDelayString = "\${offender-events.fixed-delay:100}")
    fun checkAndSendEvents() {
        try {
            service.checkAndSendEvents().forEach {
                telemetryService.trackEvent(
                    "OffenderEventPublished",
                    mapOf(
                        "crn" to it.message.crn,
                        "eventType" to it.eventType!!,
                        "occurredAt" to ISO_ZONED_DATE_TIME.format(it.message.eventDatetime),
                    ),
                )
            }
        } catch (e: Exception) {
            telemetryService.trackEvent(
                "OffenderEventsProcessingFailed",
                mapOf("Exception" to (e.message ?: "Unknown Exception")),
            )
            Sentry.captureException(e)
        }
    }
}
