package uk.gov.justice.digital.hmpps.controller.advice

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.controller.PSRContextController
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@RestControllerAdvice(basePackageClasses = [PSRContextController::class])
class ControllerAdvice {

    @ExceptionHandler(NotFoundException::class)
    fun handleException(e: NotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(status = HttpStatus.NOT_FOUND.value(), developerMessage = e.message))
    }
}
