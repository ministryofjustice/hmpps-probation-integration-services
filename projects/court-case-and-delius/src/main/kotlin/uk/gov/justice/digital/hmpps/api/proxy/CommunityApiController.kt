package uk.gov.justice.digital.hmpps.api.proxy

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException
import java.net.URI

@RestController
@RequestMapping("secure/**")
class CommunityApiController(
    @Value("\${community-api.url}") private val communityApiUrl: String,
    private val communityApiClient: CommunityApiClient
) {
    @GetMapping
    fun proxy(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<String> {

        val headers = request.headerNames.asSequence().associateWith { request.getHeader(it) }.toMutableMap()
        headers[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
        return try {
            communityApiClient.proxy(URI.create(communityApiUrl + request.pathInfo), headers)
        } catch (ex: HttpStatusCodeException) {
            ResponseEntity.status(ex.statusCode)
                .headers(ex.responseHeaders)
                .body(ex.responseBodyAsString);
        }
    }
}
