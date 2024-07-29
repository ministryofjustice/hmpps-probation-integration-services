package uk.gov.justice.digital.hmpps.api.proxy

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.json.Json
import jakarta.json.JsonPatch
import jakarta.json.JsonStructure
import jakarta.json.JsonValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpStatusCodeException
import java.io.StringReader
import java.net.URI
import kotlin.reflect.KParameter

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
        val function = instance::class.members.firstOrNull { it.name == uri.ccdFunction }
        val functionParams = function?.parameters?.drop(1)?.associateBy({ it }, {
            generateValue(it, compare.params, uri.urlParams)
        })!!
        val paramsFunc = function.parameters
        val params = mapOf(paramsFunc[0] to instance) + functionParams

        return mapper.writeValueAsString(
            function.callBy(params)
        )
    }

    fun generateValue(param: KParameter, originalValue: Map<*, *>, paramNames: List<String>): Any? {
        var value = originalValue.values.toList()[param.index - 1]
        if (value == "?" || value == "") {
            val name = paramNames[param.index - 1]
            throw DataNotAvailableException(name)
        }
        if (param.type.classifier == List::class) {
            value = value.toString().split(",")
        }
        return value
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun compare(compare: Compare, headers: Map<String, String>): CompareReport {

        val uri = Uri.valueOf(compare.uri)
        val comApiUri = compare.params.entries.fold(uri.comApiUrl) { path, (key, value) ->
            path.replace(
                "{$key}",
                value.toString()
            )
        }.replace(" ", "%20")

        val ccdJsonString = try {
            getCcdJson(compare)
        } catch (ex: DataNotAvailableException) {
            return CompareReport(
                endPointName = uri.name,
                url = comApiUri,
                message = ex.message!! + " for ${compare.params["crn"]}",
                success = false
            )
        }

        val comApiJsonString = communityApiClient.proxy(URI.create(communityApiUrl + comApiUri), headers).body!!
        val ccdJson = Json.createReader(StringReader(ccdJsonString)).readValue() as JsonStructure
        val comApiJson = Json.createReader(StringReader(comApiJsonString)).readValue() as JsonStructure
        val diff: JsonPatch = Json.createDiff(ccdJson, comApiJson)
        val results = diff.toDiffReport(ccdJson)

        return CompareReport(
            endPointName = uri.name,
            message = "${results.size} differences found between New API and Community API",
            issues = results,
            url = comApiUri,
            success = results.isEmpty(),
            testExecuted = true
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

fun JsonStructure.getValueAsString(path: String, removeQuotes: Boolean = true): String {
    val index = if (removeQuotes) 1 else 0
    return getValue(path.substring(index, path.length - index)).toString()
}

fun JsonValue.getValueAsString(path: String) = asJsonObject()[path].toString()

fun JsonPatch.toDiffReport(jsonObject: JsonStructure) = toJsonArray().map {
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
