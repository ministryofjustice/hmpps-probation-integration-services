package uk.gov.justice.digital.hmpps.api.proxy

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.json.Json
import jakarta.json.JsonObject
import jakarta.json.JsonPatch
import jakarta.json.JsonValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import java.io.StringReader
import java.net.URI

@Service
class CommunityApiService(
    @Value("\${community-api.url}") private val communityApiUrl: String,
    private val mapper: ObjectMapper,
    private val communityApiClient: CommunityApiClient,
    private val applicationContext: ApplicationContext
) {
    fun getCcdJson(compare: Compare): String {
        val uri = Uri.valueOf(compare.uri)
        val instance = applicationContext.getBean(uri.ccdInstance)
        return mapper.writeValueAsString(instance::class.members.firstOrNull { it.name == uri.ccdFunction }
            ?.call(instance, compare.crn))
    }

    fun compare(compare: Compare, headers: MutableMap<String, String>): CompareReport {

        val uri = Uri.valueOf(compare.uri)
        val ccdJsonString = getCcdJson(compare)
        val comApiUri = uri.comApiUrl.replace("{crn}", compare.crn)
        val comApiJsonString = communityApiClient.proxy(URI.create(communityApiUrl + comApiUri), headers).body!!
        val ccdJson = Json.createReader(StringReader(ccdJsonString)).readValue().asJsonObject()
        val comApiJson = Json.createReader(StringReader(comApiJsonString)).readValue().asJsonObject()
        val diff: JsonPatch = Json.createDiff(ccdJson, comApiJson)
        val results = diff.toDiffReport(ccdJson)

        return CompareReport(
            endPointName = uri.name,
            message = "${results.size} differences found between New API and Community API",
            issues = results,
            url = comApiUri,
            success = results.isEmpty()
        )
    }

    fun proxy(requestUri: String, headers: MutableMap<String, String>): ResponseEntity<String> {

        return try {
            val resp = communityApiClient.proxy(URI.create(communityApiUrl + requestUri), headers)
            CommunityApiController.log.info("returned status ${resp.statusCode} from community-api")
            return resp
        } catch (ex: HttpStatusCodeException) {
            CommunityApiController.log.error("Exception thrown when calling ${communityApiUrl + requestUri}. community-api returned ${ex.message}")
            ResponseEntity.status(ex.statusCode)
                .headers(ex.responseHeaders)
                .body(ex.responseBodyAsString)
        }
    }
}

fun JsonObject.getValueAsString(path: String, removeQuotes: Boolean = true): String {
    val index = if (removeQuotes) 1 else 0
    return getValue(path.substring(index, path.length - index)).toString()
}

fun JsonValue.getValueAsString(path: String) = asJsonObject()[path].toString()

fun JsonPatch.toDiffReport(jsonObject: JsonObject) = toJsonArray().map {
    val op = it.getValueAsString("op")
    val path = it.getValueAsString("path")
    if (op.contains("replace")) {
        val ccdApiValue = jsonObject.getValueAsString(path)
        "Values differ at ${it.getValueAsString("path")} \n" +
            "Found $ccdApiValue in new API, but is ${it.getValueAsString("value")} in Community API"
    } else if (op.contains("remove")) {
        "Additional element at $path exists in new API but is not present in Community API"
    } else if (op.contains("add")) {
        "Element at $path not found in new API, but is present with value of ${it.getValueAsString("value")} in Community API "
    } else {
        "Unhandled operation $op"
    }
}
