package uk.gov.justice.digital.hmpps.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ApplicationEvent
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent
import org.springframework.core.env.get

class WireMockInitialiser : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(WireMockInitialiser::class.java)
    }

    override fun initialize(ctx: ConfigurableApplicationContext) {
        if (ctx.environment.getProperty("wiremock.enabled", Boolean::class.java) != true) return

        val wmPort = if (ctx.environment.activeProfiles.contains("integration-test")) {
            0
        } else {
            ctx.environment["wiremock.port"]?.toInt() ?: 0
        }

        val wireMockServer = WireMockServer(
            WireMockConfiguration()
                .port(wmPort)
                .usingFilesUnderClasspath("simulations")
                .maxLoggedResponseSize(100_000)
        )
        wireMockServer.start()

        log.info("WireMock server started on port ${wireMockServer.port()}")

        ctx.beanFactory
            .registerSingleton("wireMockServer", wireMockServer)

        ctx.addApplicationListener { applicationEvent: ApplicationEvent ->
            if (applicationEvent is ContextClosedEvent) {
                wireMockServer.stop()
            }
        }

        ctx.environment.systemProperties["wiremock.port"] = wireMockServer.port().toString()
    }
}