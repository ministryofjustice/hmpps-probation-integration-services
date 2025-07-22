package uk.gov.justice.digital.hmpps.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import com.microsoft.applicationinsights.telemetry.TelemetryContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.util.UriUtils
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
        log.debug(
            "{} {} {}",
            UriUtils.encode(name, Charset.defaultCharset()),
            UriUtils.encode(properties.toString(), Charset.defaultCharset()),
            UriUtils.encode(metrics.toString(), Charset.defaultCharset())
        )
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
        log.debug(
            "{} {} {}",
            UriUtils.encode(exception.message ?: "", Charset.defaultCharset()),
            UriUtils.encode(properties.toString(), Charset.defaultCharset()),
            UriUtils.encode(metrics.toString(), Charset.defaultCharset())
        )
        telemetryClient.trackException(
            exception,
            properties.filterValues { it != null },
            metrics.filterValues { it != null }
        )
    }

    fun getContext(): TelemetryContext = telemetryClient.context
}
