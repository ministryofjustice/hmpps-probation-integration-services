package uk.gov.justice.digital.hmpps.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Async
@Service
class TelemetryService(private val telemetryClient: TelemetryClient = TelemetryClient()) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    fun trackEvent(name: String, properties: Map<String, String> = mapOf(), metrics: Map<String, Double> = mapOf()) {
        if (telemetryClient.isDisabled) log.debug("$name $properties $metrics")
        else telemetryClient.trackEvent(name, properties, metrics)
    }
}
