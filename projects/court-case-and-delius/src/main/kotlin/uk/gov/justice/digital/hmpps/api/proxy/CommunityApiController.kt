package uk.gov.justice.digital.hmpps.api.proxy

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import uk.gov.justice.digital.hmpps.api.resource.ProbationRecordResource
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import java.io.StringReader
import java.net.URI
import javax.json.Json
import javax.json.JsonObject
import javax.json.JsonPatch

@RestController

@RequestMapping("secure")
class CommunityApiController(
    @Value("\${community-api.url}") private val communityApiUrl: String,
    private val probationRecordResource: ProbationRecordResource,
    private val featureFlags: FeatureFlags,
    private val communityApiClient: CommunityApiClient,
    private val compareService: CompareService,
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
        return try {
            val resp = communityApiClient.proxy(URI.create(communityApiUrl + request.requestURI), headers)
            log.info("returned status ${resp.statusCode} from community-api")
            return resp
        } catch (ex: HttpStatusCodeException) {
            log.error("Exception thrown when calling ${communityApiUrl + request.requestURI}. community-api returned ${ex.message}")
            ResponseEntity.status(ex.statusCode)
                .headers(ex.responseHeaders)
                .body(ex.responseBodyAsString)
        }
    }

    @PostMapping("/compare")
    fun compare(@RequestBody compare: Compare, request: HttpServletRequest): CompareReport {
        val headers = request.headerNames.asSequence().associateWith { request.getHeader(it) }
            .filter { it.key != "Content-Length" }.toMutableMap()
        val uri = try {
            Uri.valueOf(compare.uri)
        } catch (ex: Exception) {
            return compare.toReport("This endpoint is not configured")
        }
        val ccdJsonString = try {
            compareService.toJsonString(compare)
        } catch (ex: Exception) {
            return compare.toReport("${uri.ccdFunction} bean cannot be found. Has this been implemented yet?")
        }
        val comApiUri = uri.comApiUrl.replace("{crn}", compare.crn)
        val comApiJsonString = try {
            communityApiClient.proxy(URI.create(communityApiUrl + comApiUri), headers).body!!
        } catch (ex: HttpStatusCodeException) {
            log.error("Exception thrown when calling ${communityApiUrl + request.requestURI}. community-api returned ${ex.message}")
            return compare.toReport(ex.message ?: "No message")
        }
        val ccdJson = Json.createReader(StringReader(ccdJsonString)).readValue().asJsonObject()
        val comApiJson = Json.createReader(StringReader(comApiJsonString)).readValue().asJsonObject()
        val diff: JsonPatch = Json.createDiff(ccdJson, comApiJson)

        val results = diff.toJsonArray().asSequence().associateWith {
            val op = it.asJsonObject()["op"].toString()
            val path = it.asJsonObject()["path"].toString()
            if (op.contains("replace")) {
                val ccdApiValue = getValueFromJsonPath(ccdJson, path)
                "Values differ at ${it.asJsonObject()["path"].toString()} \n" +
                    "Found $ccdApiValue in new API, but is ${it.asJsonObject()["value"].toString()} in Community API"
            } else if (op.contains("remove")) {
                "Additional element at $path exists in new API but is not present in Community API"
            } else if (op.contains("add")) {
                "Element at $path not found in new API, but is present with value of ${it.asJsonObject()["value"].toString()} in Community API "
            } else {
                "Unhandled operation $op"
            }
        }.map { x -> x.value }.toList()

        return CompareReport(
            endPointName = uri.name,
            message = "${results.size} differences found between New API and Community API",
            issues = results,
            url = comApiUri,
            success = results.isEmpty()
        )
    }

    fun getValueFromJsonPath(json: JsonObject, path: String): String {
        return json.getValue(path.substring(1, path.length - 1)).toString()
    }
}

fun Compare.toReport(message: String) = CompareReport(message = message, success = false, endPointName = uri)


