package uk.gov.justice.digital.hmpps.api.allocationdemand

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class RestExceptionHandler : ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errors = ErrorsResponse(
            (request as ServletWebRequest).request.requestURI,
            ex.bindingResult.fieldErrors.map { FieldError(it.code, it.defaultMessage, it.field) },
            HttpStatus.BAD_REQUEST
        )
        return ResponseEntity(errors, errors.status)
    }
}

class ErrorsResponse(val path: String, val fieldErrors: List<FieldError>, val status: HttpStatus)

data class FieldError(
    val type: String?,
    val message: String?,
    val field: String?
)
