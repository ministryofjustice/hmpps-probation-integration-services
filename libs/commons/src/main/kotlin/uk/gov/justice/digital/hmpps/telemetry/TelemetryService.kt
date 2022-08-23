package uk.gov.justice.digital.hmpps.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.message.HmppsEvent

@Async
@Service
class TelemetryService(private val telemetryClient: TelemetryClient = TelemetryClient()) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    fun trackEvent(name: String, properties: Map<String, String> = mapOf(), metrics: Map<String, Double> = mapOf()) {
        if (telemetryClient.isDisabled) log.debug("$name $properties $metrics")
        telemetryClient.trackEvent(name, properties, metrics)
    }

    fun hmppsEventReceived(hmppsEvent: HmppsEvent) {
        trackEvent(
            "${hmppsEvent.eventType.uppercase().replace(".", "_")}_RECEIVED",
            mapOf("eventType" to hmppsEvent.eventType) +
                (hmppsEvent.detailUrl?.let { mapOf("detailUrl" to it) } ?: mapOf()) +
                (hmppsEvent.personReference.identifiers.associate { Pair(it.type, it.value) })
        )
    }
}
