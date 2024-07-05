package uk.gov.justice.digital.hmpps.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import com.microsoft.applicationinsights.telemetry.TelemetryContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class TelemetryService(private val telemetryClient: TelemetryClient = TelemetryClient()) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    @Async
    fun trackEvent(name: String, properties: Map<String, String> = mapOf(), metrics: Map<String, Double> = mapOf()) {
        log.debug("{} {} {}", name, properties, metrics)
        telemetryClient.trackEvent(name, properties, metrics)
    }

    @Async
    fun trackException(
        exception: Exception,
        properties: Map<String, String> = mapOf(),
        metrics: Map<String, Double> = mapOf()
    ) {
        log.debug("{} {} {}", exception.message, properties, metrics)
        telemetryClient.trackException(exception, properties, metrics)
    }

    fun getContext(): TelemetryContext = telemetryClient.context
}
