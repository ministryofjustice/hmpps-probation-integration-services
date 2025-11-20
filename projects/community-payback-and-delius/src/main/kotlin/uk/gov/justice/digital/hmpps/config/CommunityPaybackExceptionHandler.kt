package uk.gov.justice.digital.hmpps.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.advice.FieldError
import uk.gov.justice.digital.hmpps.exceptions.LimitedAccessException

@RestControllerAdvice
class CommunityPaybackExceptionHandler {
    @ExceptionHandler(LimitedAccessException::class)
    fun handleLimitedAccessException(e: LimitedAccessException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse(
                    status = HttpStatus.FORBIDDEN.value(),
                    message = "Access denied: ${e.message}",
                    fields = listOf(
                        FieldError(type = "Access Denied", field = "crn", message = e.crn),
                        FieldError(type = "Access Denied", field = "username", message = e.username),
                    )
                )
            )
    }
}