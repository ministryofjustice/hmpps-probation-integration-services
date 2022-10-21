package uk.gov.justice.digital.hmpps.controller.advice

import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.controller.ApiController
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@RestControllerAdvice(basePackageClasses = [ApiController::class])
class ControllerAdvice {
    @ExceptionHandler(NotFoundException::class)
    fun handleException(e: NotFoundException) = ResponseEntity
        .status(NOT_FOUND)
        .body(ErrorResponse(status = NOT_FOUND.value(), message = e.message))
}
