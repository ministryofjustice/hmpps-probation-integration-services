package uk.gov.justice.digital.hmpps.api.proxy

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.resource.ConvictionResource
import uk.gov.justice.digital.hmpps.api.resource.ProbationRecordResource
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

@RestController
@PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
@RequestMapping("secure")
class CommunityApiController(
    private val probationRecordResource: ProbationRecordResource,
    private val featureFlags: FeatureFlags,
    private val communityApiService: CommunityApiService,
    private val convictionResource: ConvictionResource,
    private val taskExecutor: ThreadPoolTaskExecutor,
    private val personRepository: PersonEventRepository
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

    @GetMapping("/offenders/crn/{crn}/allOffenderManagers")
    fun offenderManagers(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @RequestParam(defaultValue = "false", required = false) includeProbationAreaTeams: Boolean
    ): Any {

        if (featureFlags.enabled("ccd-offender-managers-enabled")) {
            return probationRecordResource.getAllOffenderManagers(crn, includeProbationAreaTeams)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions")
    fun convictions(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @RequestParam(defaultValue = "false", required = false) activeOnly: Boolean
    ): Any {

        if (featureFlags.enabled("ccd-convictions-enabled")) {
            return convictionResource.getConvictionsForOffenderByCrn(crn, activeOnly)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}")
    fun convictionForOffenderByCrnAndConvictionId(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable convictionId: Long
    ): Any? {

        if (featureFlags.enabled("ccd-conviction-by-id-enabled")) {
            return convictionResource.getConvictionForOffenderByCrnAndConvictionId(crn, convictionId)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/requirements")
    fun convictionRequirements(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
        @RequestParam(required = false, defaultValue = "true") activeOnly: Boolean,
        @RequestParam(required = false, defaultValue = "true") excludeSoftDeleted: Boolean
    ): Any {

        if (featureFlags.enabled("ccd-conviction-requirements-enabled")) {
            return convictionResource.getRequirementsForConviction(crn, convictionId, activeOnly, excludeSoftDeleted)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/nsis")
    fun nsisByCrnAndConvictionId(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
        @RequestParam(required = true) nsiCodes: List<String>,
    ): Any {

        if (featureFlags.enabled("ccd-conviction-nsis-enabled")) {
            return convictionResource.getNsisByCrnAndConvictionId(crn, convictionId, nsiCodes)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/pssRequirements")
    fun pssByCrnAndConvictionId(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ): Any {

        if (featureFlags.enabled("ccd-conviction-pss-enabled")) {
            return convictionResource.getPssRequirementsByConvictionId(crn, convictionId)
        }
        return proxy(request)
    }

    @GetMapping("/**")
    fun proxy(request: HttpServletRequest): ResponseEntity<String> {
        val headers = request.headerNames.asSequence().associateWith { request.getHeader(it) }.toMutableMap()
        val fullUri =
            if (request.queryString != null) request.requestURI + '?' + request.queryString else request.requestURI
        return communityApiService.proxy(fullUri, headers)
    }

    @PostMapping("/compare")
    fun compare(@RequestBody compare: Compare, request: HttpServletRequest): CompareReport {
        val headers = mapOf(HttpHeaders.AUTHORIZATION to request.getHeader(HttpHeaders.AUTHORIZATION))
        return communityApiService.compare(compare, headers)
    }

    @PostMapping("/compareAll")
    fun compareAll(@RequestBody compare: CompareAll, request: HttpServletRequest): CompareAllReport {
        val headers = mapOf(HttpHeaders.AUTHORIZATION to request.getHeader(HttpHeaders.AUTHORIZATION))
        val pageable = PageRequest.of(compare.pageNumber - 1, compare.pageSize)
        val personList =
            if (compare.crns.isNullOrEmpty()) personRepository.findAllCrns(pageable) else personRepository.findByCrnIn(
                compare.crns,
                pageable
            )

        val reports = personList.content.flatMap { person ->
            val convictionId = person.events.filter { it.disposal != null }.maxOfOrNull { it.id }
            val nsiCodes = person.nsis.filter { it.eventId == convictionId }.map { it.type.code.trim() }
            runAll(person.crn, convictionId, nsiCodes, compare, headers).stream()
                .map(CompletableFuture<CompareReport>::join)
                .collect(Collectors.toList())
        }
        val unsuccessful = reports.filter { !it.success }

        return CompareAllReport(
            totalNumberOfCrns = personList.numberOfElements,
            totalPages = pageable.pageSize,
            currentPageNumber = compare.pageNumber,
            totalNumberOfRequests = reports.size,
            numberOfSuccessfulRequests = reports.size - unsuccessful.size,
            numberOfUnsuccessfulRequests = unsuccessful.size,
            failureReports = reports.filter { !it.success }
        )
    }

    fun setParams(map: Map<String, Any>, convictionId: Long?, nsiCodes: List<String>): Map<String, Any> {
        val new = map.toMutableMap()
        if (map.containsKey("convictionId") && convictionId != null) {
            new["convictionId"] = convictionId
        }
        if (map.containsKey("nsiCodes")) {
            new["nsiCodes"] = nsiCodes.joinToString(",")
        }
        return new
    }

    fun runAll(
        crn: String,
        convictionId: Long?,
        nsiCodes: List<String>,
        compare: CompareAll,
        headers: Map<String, String>
    ): List<CompletableFuture<CompareReport>> {
        return compare.uriConfig.entries.map {
            val params = setParams(it.value, convictionId, nsiCodes)
            CompletableFuture.supplyAsync(
                {
                    communityApiService.compare(
                        Compare(params = mapOf("crn" to crn) + params, uri = it.key),
                        headers
                    )
                }, taskExecutor
            )
        }
    }
}

