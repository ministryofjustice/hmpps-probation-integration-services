package uk.gov.justice.digital.hmpps.test

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.module.SimpleModule
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.security.TokenHelper
import java.time.ZonedDateTime

object MockMvcExtensions {
    val objectMapper = jsonMapper {
        addModule(kotlinModule())
        addModule(SimpleModule().addDeserializer(ZonedDateTime::class.java, ZonedDateTimeDeserializer()))
        configure(SerializationFeature.INDENT_OUTPUT, true)
        changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
    }

    fun MockHttpServletRequestBuilder.withToken() =
        header(HttpHeaders.AUTHORIZATION, "Bearer ${TokenHelper.TOKEN}")

    fun MockHttpServletRequestBuilder.withJson(jsonBody: Any) =
        this.contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(jsonBody))

    var MockHttpServletRequestDsl.json
        get(): Any = NotImplementedError()
        set(jsonBody) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(jsonBody)
        }

    fun MockHttpServletRequestDsl.withToken() =
        header(HttpHeaders.AUTHORIZATION, "Bearer ${TokenHelper.TOKEN}")

    inline fun <reified T> MockHttpServletResponse.contentAsJson(): T = objectMapper.readValue<T>(this.contentAsString)

    inline fun <reified T> ResultActions.andExpectJson(obj: T, compareMode: JsonCompareMode = JsonCompareMode.LENIENT) =
        this.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(objectMapper.writeValueAsString(obj), compareMode))

    inline fun <reified T> ResultActionsDsl.andExpectJson(
        obj: T,
        compareMode: JsonCompareMode = JsonCompareMode.LENIENT
    ) = andExpect {
        header {
            string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        }
        content {
            json(
                objectMapper.writeValueAsString(obj),
                compareMode
            )
        }
    }
}
