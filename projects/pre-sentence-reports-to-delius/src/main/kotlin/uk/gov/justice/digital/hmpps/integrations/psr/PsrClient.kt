package uk.gov.justice.digital.hmpps.integrations.psr

import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface PsrClient {
    @GetExchange
    fun getPsrReport(baseUrl: URI): ByteArray
}
