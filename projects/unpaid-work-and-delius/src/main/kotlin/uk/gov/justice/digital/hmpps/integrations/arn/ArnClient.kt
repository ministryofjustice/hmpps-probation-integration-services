package uk.gov.justice.digital.hmpps.integrations.arn

import org.springframework.http.ResponseEntity
import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface ArnClient {
    @GetExchange
    fun getUPWAssessment(baseUrl: URI): ResponseEntity<ByteArray>
}
