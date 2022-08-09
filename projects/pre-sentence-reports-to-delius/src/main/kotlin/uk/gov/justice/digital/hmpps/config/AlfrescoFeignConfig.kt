package uk.gov.justice.digital.hmpps.config

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean

class AlfrescoFeignConfig {
    @Bean
    fun requestInterceptor() = RequestInterceptor { template ->
            template.header("X-DocRepository-Remote-User", "N00")
            template.header("X-DocRepository-Real-Remote-User", "pre-sentence-reports-to-delius")
            template.header("Content-Type: multipart/form-data")
    }
}