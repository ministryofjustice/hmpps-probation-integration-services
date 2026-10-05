package uk.gov.justice.digital.hmpps.advice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class CommunityPaybackControllerAdviceTest {

    private val advice = CommunityPaybackControllerAdvice()

    @Test
    fun `handleNoSuchElement returns 404 error response`() {
        val response = advice.handleNoSuchElement(NoSuchElementException("Document not found with id: 456"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body).isEqualTo(
            ErrorResponse(status = HttpStatus.NOT_FOUND.value(), message = "Document not found with id: 456")
        )
    }
}

