package uk.gov.justice.digital.hmpps.aws

import io.awspring.cloud.core.region.StaticRegionProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DevAwsConfig {
    @Bean
    fun regionProvider() = StaticRegionProvider("eu-west-2") // Disable region lookup in dev mode
}
