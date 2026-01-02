package uk.gov.justice.digital.hmpps.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(DevConfig.DevWireMockConfig::class, DevConfig.DevDataLoaderConfig::class)
class DevConfig {
    @ConfigurationProperties(prefix = "dev.wiremock")
    class DevWireMockConfig(val enabled: Boolean = false, val port: Int? = null)

    @ConfigurationProperties(prefix = "dev.dataloader")
    class DevDataLoaderConfig(val enabled: Boolean = false)
}