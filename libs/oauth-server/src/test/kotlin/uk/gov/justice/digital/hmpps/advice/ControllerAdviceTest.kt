package uk.gov.justice.digital.hmpps.advice

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus.*
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import kotlin.reflect.jvm.javaMethod

class ControllerAdviceTest {
    @Test
    fun `handles not found`() {
        val response = ControllerAdvice().handle(NotFoundException("Some Entity", "identifier", 123))

        assertThat(response.statusCode, equalTo(NOT_FOUND))
        assertThat(response.body?.message, equalTo("Some Entity with identifier of 123 not found"))
    }

    @Test
    fun `handles conflict`() {
        val response = ControllerAdvice().handle(ConflictException("something conflicted"))

        assertThat(response.statusCode, equalTo(CONFLICT))
        assertThat(response.body?.message, equalTo("something conflicted"))
    }

    @Test
    fun `handles database integrity violation`() {
        val response = JpaControllerAdvice().handle(DataIntegrityViolationException("something conflicted"))

        assertThat(response.statusCode, equalTo(CONFLICT))
        assertThat(response.body?.message, equalTo("something conflicted"))
    }

    @Test
    fun `handles invalid method argument`() {
        val bindingResult = mock(BindingResult::class.java)
        whenever(bindingResult.fieldErrors).thenReturn(listOf(FieldError("object", "field", "message")))
        val response = ControllerAdvice().handle(
            MethodArgumentNotValidException(MethodParameter(::testMethod.javaMethod!!, 0), bindingResult)
        )

        assertThat(response.statusCode, equalTo(BAD_REQUEST))
        assertThat(response.body?.message, equalTo("Validation failure"))
        assertThat(response.body?.fields, equalTo(listOf(FieldError(null, "message", "field"))))
    }

    private fun testMethod(parameter: String) = parameter
}
