package uk.gov.justice.digital.hmpps.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
@Profile("dev", "integration-test")
@ConditionalOnProperty("wiremock.enabled", havingValue = "true", matchIfMissing = false)
class WiremockService(
    @Value("\${wiremock.port:7979}") private val port: Int,
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(WiremockService::class.java)
    }

    private val wiremockServer = WireMockServer(
        options()
            .port(port)
            .usingFilesUnderClasspath("simulations")
    )

    @PostConstruct
    fun init() {
        log.info("starting wiremock server ...")
        wiremockServer.start()
    }

    @PreDestroy
    fun shutdown() {
        log.info("shutting down wiremock server ...")
        wiremockServer.shutdown()
    }
}
