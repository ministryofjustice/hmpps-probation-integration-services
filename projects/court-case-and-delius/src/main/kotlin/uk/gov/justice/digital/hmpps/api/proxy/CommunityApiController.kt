package uk.gov.justice.digital.hmpps.api.proxy

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.resource.ProbationRecordResource
import uk.gov.justice.digital.hmpps.flags.FeatureFlags

@RestController
@PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
@RequestMapping("secure")
class CommunityApiController(
    private val probationRecordResource: ProbationRecordResource,
    private val featureFlags: FeatureFlags,
    private val communityApiService: CommunityApiService,
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/offenders/crn/{crn}/all")
    fun offenderDetail(request: HttpServletRequest, @PathVariable crn: String): Any {

        if (featureFlags.enabled("ccd-offender-detail-enabled")) {
            return probationRecordResource.getOffenderDetail(crn)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}")
    fun offenderSummary(request: HttpServletRequest, @PathVariable crn: String): Any {

        if (featureFlags.enabled("ccd-offender-summary-enabled")) {
            return probationRecordResource.getOffenderDetailSummary(crn)
        }
        return proxy(request)
    }

    @GetMapping("/**")
    fun proxy(request: HttpServletRequest): ResponseEntity<String> {
        val headers = request.headerNames.asSequence().associateWith { request.getHeader(it) }.toMutableMap()
        return communityApiService.proxy(request.requestURI, headers)
    }

    @PostMapping("/compare")
    fun compare(@RequestBody compare: Compare, request: HttpServletRequest): CompareReport {
        val headers = request.headerNames.asSequence().associateWith { request.getHeader(it) }
            .filter { it.key.lowercase() == "Authorization".lowercase() }.toMutableMap()
        return communityApiService.compare(compare, headers)
    }
}

