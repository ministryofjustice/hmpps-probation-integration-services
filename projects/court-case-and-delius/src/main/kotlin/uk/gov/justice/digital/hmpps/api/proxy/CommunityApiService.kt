package uk.gov.justice.digital.hmpps.api.proxy

import com.fasterxml.jackson.databind.ObjectMapper
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpStatusCodeException
import uk.gov.justice.digital.hmpps.api.resource.advice.CommunityApiControllerAdvice
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.lang.reflect.InvocationTargetException
import java.net.URI
import kotlin.reflect.KParameter

@Service
class CommunityApiService(
    @Value("\${community-api.url}") private val communityApiUrl: String,
    private val mapper: ObjectMapper,
    private val communityApiClient: CommunityApiClient,
    private val applicationContext: ApplicationContext,
    private val controllerAdvice: CommunityApiControllerAdvice
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

        val response = try {
            function.callBy(params)
        } catch (ex: InvocationTargetException) {
            when (val cause = ex.cause) {
                is AccessDeniedException -> controllerAdvice.handleAccessDenied(cause).body
                is NotFoundException -> controllerAdvice.handleNotFound(cause).body
                is InvalidRequestException -> controllerAdvice.handleInvalidRequest(cause).body
                else -> throw ComparisonException(ex.cause?.message)
            }
        }
        return mapper.writeValueAsString(response)
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
    fun compare(compare: Compare, headers: Map<String, String>, showValues: Boolean = false): CompareReport {

        val uri = Uri.valueOf(compare.uri)
        val comApiUri = compare.params.entries.fold(uri.comApiUrl) { path, (key, value) ->
            if (value != null) {
                path.replace(
                    "{$key}",
                    value.toString()
                )
            } else {
                path.replace("$key={$key}", "")
            }
        }.replace(" ", "%20")

        val ccdJsonString = try {
            getCcdJson(compare)
        } catch (ex: Exception) {
            when (ex) {
                is DataNotAvailableException, is ComparisonException -> {
                    return CompareReport(
                        endPointName = uri.name,
                        url = comApiUri,
                        message = ex.message!! + " for ${compare.params["crn"]}",
                        success = false
                    )
                }

                else -> throw ex
            }
        }

        val comApiJsonString = proxy(comApiUri, headers.toMutableMap()).body!!

        try {
            JSONAssert.assertEquals(
                ccdJsonString,
                String(comApiJsonString.contentAsByteArray),
                JSONCompareMode.NON_EXTENSIBLE
            )
            return CompareReport(
                endPointName = uri.name,
                url = comApiUri,
                message = "Json is equal",
                success = true,
                testExecuted = true
            )
        } catch (ex: AssertionError) {
            // Differences found other than the order
            return CompareReport(
                endPointName = uri.name,
                url = comApiUri,
                message = ex.message!!,
                success = false,
                testExecuted = true
            )
        }
    }

    fun proxy(requestUri: String, headers: MutableMap<String, String>): ResponseEntity<Resource> {

        return try {
            val resp = communityApiClient.proxy(URI.create(communityApiUrl + requestUri), headers)
            CommunityApiController.log.info("returned status ${resp.statusCode} from community-api")
            return resp
        } catch (ex: HttpStatusCodeException) {
            CommunityApiController.log.error("Exception thrown when calling ${communityApiUrl + requestUri}. community-api returned ${ex.message}")
            ResponseEntity.status(ex.statusCode)
                .headers(ex.responseHeaders)
                .body(ByteArrayResource(ex.responseBodyAsByteArray))
        }
    }
}

