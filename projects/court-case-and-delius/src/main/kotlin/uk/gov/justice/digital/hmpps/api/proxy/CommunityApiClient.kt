package uk.gov.justice.digital.hmpps.api.proxy

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.HttpExchange
import java.net.URI

interface CommunityApiClient {
    @HttpExchange(method = "GET", contentType = "application/json; charset=utf-8")
    fun proxy(url: URI, @RequestHeader headers: Map<String, String>): ResponseEntity<ByteArray>
}
