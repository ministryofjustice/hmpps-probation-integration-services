package uk.gov.justice.digital.hmpps.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import com.microsoft.applicationinsights.telemetry.TelemetryContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.Charset

@Service
class TelemetryService(private val telemetryClient: TelemetryClient = TelemetryClient()) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    @Async
    fun trackEvent(
        name: String,
        properties: Map<String, String?> = mapOf(),
        metrics: Map<String, Double?> = mapOf()
    ) {
        log.debug("{} {} {}", URLEncoder.encode(name, Charset.defaultCharset()), properties, metrics)
        telemetryClient.trackEvent(
            name,
            properties.filterValues { it != null },
            metrics.filterValues { it != null }
        )
    }

    @Async
    fun trackException(
        exception: Exception,
        properties: Map<String, String?> = mapOf(),
        metrics: Map<String, Double?> = mapOf()
    ) {
        log.debug("{} {} {}", URLEncoder.encode(exception.message, Charset.defaultCharset()), properties, metrics)
        telemetryClient.trackException(
            exception,
            properties.filterValues { it != null },
            metrics.filterValues { it != null }
        )
    }

    fun getContext(): TelemetryContext = telemetryClient.context
}
