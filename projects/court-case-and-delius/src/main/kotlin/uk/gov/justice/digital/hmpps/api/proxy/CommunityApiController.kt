package uk.gov.justice.digital.hmpps.api.proxy

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.api.resource.ConvictionResource
import uk.gov.justice.digital.hmpps.api.resource.DocumentResource
import uk.gov.justice.digital.hmpps.api.resource.ProbationRecordResource
import uk.gov.justice.digital.hmpps.api.resource.RegistrationResource
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.util.concurrent.CompletableFuture

@RestController
@PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
@RequestMapping("secure")
class CommunityApiController(
    private val probationRecordResource: ProbationRecordResource,
    private val featureFlags: FeatureFlags,
    private val communityApiService: CommunityApiService,
    private val convictionResource: ConvictionResource,
    private val registrationResource: RegistrationResource,
    private val documentResource: DocumentResource,
    private val taskExecutor: ThreadPoolTaskExecutor,
    private val personRepository: PersonEventRepository,
    private val telemetryService: TelemetryService,
    private val mapper: ObjectMapper
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/offenders/crn/{crn}/all")
    fun offenderDetail(request: HttpServletRequest, @PathVariable crn: String): Any {

        sendComparisonReport(mapOf("crn" to crn), Uri.OFFENDER_DETAIL, request)

        if (featureFlags.enabled("ccd-offender-detail-enabled")) {
            return probationRecordResource.getOffenderDetail(crn)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}")
    fun offenderSummary(request: HttpServletRequest, @PathVariable crn: String): Any {

        sendComparisonReport(mapOf("crn" to crn), Uri.OFFENDER_SUMMARY, request)

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

        sendComparisonReport(
            mapOf("crn" to crn, "includeProbationAreaTeams" to includeProbationAreaTeams),
            Uri.OFFENDER_MANAGERS,
            request
        )

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

        sendComparisonReport(mapOf("crn" to crn, "activeOnly" to activeOnly), Uri.CONVICTIONS, request)

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

        sendComparisonReport(mapOf("crn" to crn, "convictionId" to convictionId), Uri.CONVICTION_BY_ID, request)

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

        sendComparisonReport(
            mapOf("crn" to crn) +
                mapOf(
                    "convictionId" to convictionId,
                    "activeOnly" to activeOnly,
                    "excludeSoftDeleted" to excludeSoftDeleted
                ), Uri.CONVICTION_REQUIREMENTS, request
        )

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

        sendComparisonReport(
            mapOf(
                "crn" to crn,
                "convictionId" to convictionId,
                "nsiCodes" to nsiCodes
            ), Uri.CONVICTION_BY_ID_NSIS, request
        )

        if (featureFlags.enabled("ccd-conviction-nsis-enabled")) {
            return convictionResource.getNsisByCrnAndConvictionId(crn, convictionId, nsiCodes)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/attendancesFilter")
    fun convictionByIdAttendances(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ): Any {

        sendComparisonReport(
            mapOf(
                "crn" to crn,
                "convictionId" to convictionId
            ), Uri.CONVICTION_BY_ID_ATTENDANCES, request
        )

        if (featureFlags.enabled("ccd-conviction-by-id-attendances")) {
            return convictionResource.getConvictionAttendances(crn, convictionId)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/nsis/{nsiId}")
    fun nsisByNisId(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
        @PathVariable nsiId: Long,
    ): Any {

        sendComparisonReport(
            mapOf(
                "crn" to crn,
                "convictionId" to convictionId,
                "nsiId" to nsiId
            ), Uri.CONVICTION_BY_NSIS_ID, request
        )

        if (featureFlags.enabled("ccd-conviction-nsis-by-id-enabled")) {
            return convictionResource.getNsiByNsiId(crn, convictionId, nsiId)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/pssRequirements")
    fun pssByCrnAndConvictionId(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ): Any {

        sendComparisonReport(
            mapOf(
                "crn" to crn,
                "convictionId" to convictionId
            ), Uri.CONVICTION_BY_ID_PSS, request
        )

        if (featureFlags.enabled("ccd-conviction-pss-enabled")) {
            return convictionResource.getPssRequirementsByConvictionId(crn, convictionId)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/courtAppearances")
    fun convictionByIdCourtAppearances(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ): Any {

        sendComparisonReport(
            mapOf(
                "crn" to crn,
                "convictionId" to convictionId
            ), Uri.CONVICTION_BY_ID_COURT_APPEARANCES, request
        )

        if (featureFlags.enabled("ccd-conviction-by-id-court-appearances")) {
            return convictionResource.getConvictionCourtAppearances(crn, convictionId)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/courtReports")
    fun convictionByIdCourtReports(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ): Any {

        sendComparisonReport(
            mapOf(
                "crn" to crn,
                "convictionId" to convictionId
            ), Uri.CONVICTION_BY_ID_COURT_REPORTS, request
        )

        if (featureFlags.enabled("ccd-conviction-by-id-court-reports")) {
            return convictionResource.getConvictionCourtReports(crn, convictionId)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/licenceConditions")
    fun convictionByIdLicenceConditions(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ): Any {

        sendComparisonReport(
            mapOf(
                "crn" to crn,
                "convictionId" to convictionId
            ), Uri.CONVICTION_BY_ID_LICENCE_CONDITIONS, request
        )

        if (featureFlags.enabled("ccd-conviction-by-id-licence-conditions")) {
            return convictionResource.getConvictionLicenceConditions(crn, convictionId)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/convictions/{convictionId}/sentenceStatus")
    fun convictionByIdSentenceStatus(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable convictionId: Long,
    ): Any {

        sendComparisonReport(
            mapOf(
                "crn" to crn,
                "convictionId" to convictionId
            ), Uri.CONVICTION_BY_ID_SENTENCE_STATUS, request
        )

        if (featureFlags.enabled("ccd-conviction-by-id-sentence-status")) {
            return convictionResource.getConvictionSentenceStatus(crn, convictionId)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/documents/grouped")
    fun documentsGrouped(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) subtype: String?
    ): Any {

        val params = mutableMapOf<String, String>()
        type?.let { params["type"] = it }
        subtype?.let { params["subtype"] = it }
        sendComparisonReport(
            params, Uri.DOCUMENTS_GROUPED, request
        )

        if (featureFlags.enabled("ccd-document-grouped")) {
            return documentResource.getOffenderDocumentsGrouped(crn, type, subtype)
        }
        return proxy(request)
    }

    @GetMapping("/offenders/crn/{crn}/documents/{documentId}")
    fun downloadDocument(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @PathVariable documentId: String
    ): ResponseEntity<StreamingResponseBody> {

        if (featureFlags.enabled("ccd-download-document")) {
            return documentResource.getOffenderDocumentById(crn, documentId)
        }

        val proxied = proxy(request)
        val streamingResponseBody =
            StreamingResponseBody { output -> proxied.body!!.inputStream.use { it.copyTo(output) } }
        return ResponseEntity
            .status(proxied.statusCode)
            .headers(proxied.headers)
            .body(streamingResponseBody)
    }

    @GetMapping("/offenders/crn/{crn}/registrations")
    fun registrations(
        request: HttpServletRequest,
        @PathVariable crn: String,
        @RequestParam(defaultValue = "false", required = false) activeOnly: Boolean
    ): Any {
        sendComparisonReport(mapOf("crn" to crn, "activeOnly" to activeOnly), Uri.REGISTRATIONS, request)

        if (featureFlags.enabled("ccd-registrations-enabled")) {
            return registrationResource.getOffenderRegistrations(crn, activeOnly)
        }
        return proxy(request)
    }

    fun proxy(request: HttpServletRequest): ResponseEntity<Resource> {
        val headers = request.headerNames.asSequence().associateWith { request.getHeader(it) }.toMutableMap()
        val fullUri =
            if (request.queryString != null) request.requestURI + '?' + request.queryString else request.requestURI
        return communityApiService.proxy(fullUri, headers)
    }

    @PostMapping("/compare")
    fun compare(@RequestBody compare: Compare, request: HttpServletRequest): CompareReport {
        val headers = mapOf(HttpHeaders.AUTHORIZATION to request.getHeader(HttpHeaders.AUTHORIZATION))
        return communityApiService.compare(compare, headers, showValues = true)
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
            val nsiId = person.nsis.firstOrNull()?.id
            val futures = runAll(person.crn, convictionId, nsiCodes, nsiId, compare, headers).toTypedArray()
            CompletableFuture.allOf(*futures).join()
            futures.map { it.join() }.toList()
        }

        val executed = reports.filter { it.testExecuted == true }
        val successful = reports.filter { it.success && it.testExecuted == true }
        val unsuccessful = reports.filter { !it.success && it.testExecuted == true }
        val executionFailures = reports.filter { it.testExecuted == false }
        val distinctEndpointsExecuted = executed.groupBy(CompareReport::endPointName)
        val covered = distinctEndpointsExecuted.map { it.key + " covered " + it.value.size + " times" }
        val requestedButNotCovered = compare.uriConfig.entries.map { it.key }.toSet()
            .filter { e -> !distinctEndpointsExecuted.map { it.key }.toSet().contains(e) }

        return CompareAllReport(
            totalNumberOfCrns = personList.totalElements.toInt(),
            totalPages = personList.totalPages,
            currentPageNumber = compare.pageNumber,
            totalNumberOfRequests = reports.size - executionFailures.size,
            numberOfSuccessfulRequests = successful.size,
            numberOfUnsuccessfulRequests = unsuccessful.size,
            unableToBeExecuted = executionFailures.size,
            failureReports = unsuccessful,
            endpointsCovered = covered,
            endpointsRequestedButNotCovered = requestedButNotCovered
        )
    }

    fun setParams(map: Map<String, Any>, convictionId: Long?, nsiCodes: List<String>, nsiId: Long?): Map<String, Any> {
        val new = map.toMutableMap()
        if (map.containsKey("nsiId") && nsiId != null) {
            new["nsiId"] = nsiId
        }
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
        nsiId: Long?,
        compare: CompareAll,
        headers: Map<String, String>
    ): List<CompletableFuture<CompareReport>> {
        return compare.uriConfig.entries.map {
            val params = setParams(it.value, convictionId, nsiCodes, nsiId)
            CompletableFuture.supplyAsync(
                {
                    communityApiService.compare(
                        Compare(params = mapOf("crn" to crn) + params, uri = it.key),
                        headers,
                        showValues = true
                    )
                }, taskExecutor
            )
        }
    }

    fun sendComparisonReport(params: Map<String, Any>, uri: Uri, request: HttpServletRequest) {
        CompletableFuture.supplyAsync({
            val headers = mapOf(HttpHeaders.AUTHORIZATION to request.getHeader(HttpHeaders.AUTHORIZATION))
            val compare = Compare(params = params, uri = uri.name)
            val report = communityApiService.compare(compare, headers)
            telemetryService.comparisonFailureEvent(
                params["crn"].toString(),
                compare,
                report,
                request.requestURL.toString().replace(request.requestURI, "")
            )
        }, taskExecutor)
    }

    fun TelemetryService.comparisonFailureEvent(
        crn: String,
        compare: Compare,
        compareReport: CompareReport,
        baseUrl: String
    ) {
        if (!compareReport.success) {
            val comparePayload = mapper.writeValueAsString(compare)
            trackEvent(
                "ComparisonFailureEvent",
                mapOf(
                    "crn" to crn,
                    "endpointName" to compareReport.endPointName,
                    "url" to "$baseUrl/${compareReport.url!!}",
                    "numberOfDifferences" to compareReport.issues?.size.toString(),
                    "compareUrl" to "${baseUrl}secure/compare",
                    "comparePayload" to comparePayload
                )
            )
        }
    }
}

