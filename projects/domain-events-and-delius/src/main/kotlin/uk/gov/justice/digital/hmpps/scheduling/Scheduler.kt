package uk.gov.justice.digital.hmpps.scheduling

import io.sentry.Sentry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.service.DomainEventService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class Scheduler(
    private val domainEventService: DomainEventService,
    private val telemetryService: TelemetryService,
) {
    @Scheduled(fixedDelayString = "\${poller.fixed-delay:100}")
    fun poll() {
        try {
            val count = domainEventService.publishBatch()
            if (count > 0) telemetryService.trackEvent("DomainEventsProcessed", mapOf("EventsSent" to count.toString()))
        } catch (e: Exception) {
            telemetryService.trackException(e)
            Sentry.captureException(e)
            throw e
        }
    }
}
