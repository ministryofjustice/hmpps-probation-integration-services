package uk.gov.justice.digital.hmpps.integrations.delius

import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaService

@Service
class OffenderDeltaPoller(private val service: OffenderDeltaService) {
    @Scheduled(fixedDelayString = "\${offender-events.fixed-delay:100}")
    @WithSpan("POLL offender_delta", kind = SpanKind.SERVER)
    fun poll() {
        service.prepareNotificationsAndDeleteDeltas()
            .also { it.forEach(service::notify) }
    }
}
