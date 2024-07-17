package uk.gov.justice.digital.hmpps.api.proxy

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException
import uk.gov.justice.digital.hmpps.api.resource.ProbationRecordResource
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import java.net.URI

@RestController
@RequestMapping("secure")
class CommunityApiController(
    @Value("\${community-api.url}") private val communityApiUrl: String,
    private val probationRecordResource: ProbationRecordResource,
    private val featureFlags: FeatureFlags,
    private val communityApiClient: CommunityApiClient
) {

    @GetMapping("/offenders/crn/{crn}/all")
    fun offenderDetail(request: HttpServletRequest, response: HttpServletResponse, @PathVariable crn: String): Any {

        if (featureFlags.enabled("ccd-offender-detail-enabled")) {
            return probationRecordResource.getOffenderDetail(crn)
        }
        return proxy(request, response)
    }

    @GetMapping("/offenders/crn/{crn}")
    fun offenderSummary(request: HttpServletRequest, response: HttpServletResponse, @PathVariable crn: String): Any {

        if (featureFlags.enabled("ccd-offender-summary-enabled")) {
            return probationRecordResource.getOffenderDetailSummary(crn)
        }
        return proxy(request, response)
    }

    @GetMapping("/**")
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
