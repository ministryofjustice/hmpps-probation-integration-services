package uk.gov.justice.digital.hmpps.wiremock

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.get
import org.springframework.core.env.getProperty
import org.springframework.test.util.TestSocketUtils

class WireMockInitialiser : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(ctx: ConfigurableApplicationContext) {
        if (ctx.environment.getProperty<Boolean>("dev.wiremock.enabled") != true) return
        ctx.environment.systemProperties["dev.wiremock.port"] = ctx.environment["dev.wiremock.port"]?.toInt()
            ?.takeIf { !ctx.environment.activeProfiles.contains("integration-test") }
            ?: TestSocketUtils.findAvailableTcpPort()
    }
}