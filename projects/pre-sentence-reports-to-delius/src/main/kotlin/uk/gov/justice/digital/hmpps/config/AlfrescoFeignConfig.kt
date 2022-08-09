package uk.gov.justice.digital.hmpps.config

import feign.RequestInterceptor

class AlfrescoFeignConfig {
    fun requestInterceptor() = RequestInterceptor { template ->
            template.header("X-DocRepository-Remote-User", "N00")
            template.header("X-DocRepository-Real-Remote-User", "pre-sentence-reports-to-delius")
    }
}