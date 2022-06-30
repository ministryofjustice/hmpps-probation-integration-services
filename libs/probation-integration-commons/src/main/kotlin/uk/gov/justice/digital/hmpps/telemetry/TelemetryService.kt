package uk.gov.justice.digital.hmpps.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Async
@Service
class TelemetryService(private val telemetryClient: TelemetryClient = TelemetryClient()) {
    fun trackEvent(name: String, properties: Map<String, String> = mapOf(), metrics: Map<String, Double> = mapOf()) {
        telemetryClient.trackEvent(name, properties, metrics)
    }
}
