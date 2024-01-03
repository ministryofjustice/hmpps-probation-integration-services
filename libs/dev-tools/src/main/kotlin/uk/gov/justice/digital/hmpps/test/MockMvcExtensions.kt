package uk.gov.justice.digital.hmpps.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.security.TokenHelper
import java.time.ZonedDateTime

object MockMvcExtensions {
    val objectMapper: ObjectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .registerModule(JavaTimeModule())
        .registerModule(SimpleModule().addDeserializer(ZonedDateTime::class.java, ZonedDateTimeDeserializer()))
        .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)

    fun MockHttpServletRequestBuilder.withToken() =
        header(HttpHeaders.AUTHORIZATION, "Bearer ${TokenHelper.TOKEN}")

    fun MockHttpServletRequestBuilder.withJson(jsonBody: Any) =
        this.contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(jsonBody))

    inline fun <reified T> MockHttpServletResponse.contentAsJson(): T = objectMapper.readValue<T>(this.contentAsString)

    inline fun <reified T> ResultActions.andExpectJson(obj: T, strict: Boolean = false) =
        this.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(objectMapper.writeValueAsString(obj), strict))
}
