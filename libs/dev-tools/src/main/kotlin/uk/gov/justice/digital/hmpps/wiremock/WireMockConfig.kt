package uk.gov.justice.digital.hmpps.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order

@Configuration
@Order(HIGHEST_PRECEDENCE)
@ConditionalOnProperty("dev.wiremock.enabled")
class WireMockConfig {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(WireMockConfig::class.java)
    }

    @Bean(destroyMethod = "stop")
    fun wireMockServer(@Value($$"${dev.wiremock.port}") port: Int) = WireMockServer(
        options()
            .port(port)
            .usingFilesUnderClasspath("simulations")
            .maxLoggedResponseSize(100_000)
    ).apply {
        start()
        log.info("WireMock server started on port ${port()}")
    }
}