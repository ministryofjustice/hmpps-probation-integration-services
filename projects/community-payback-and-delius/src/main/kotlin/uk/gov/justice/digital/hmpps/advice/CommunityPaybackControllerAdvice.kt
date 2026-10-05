package uk.gov.justice.digital.hmpps.advice

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.controller.AppointmentsController
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@RestControllerAdvice(basePackageClasses = [AppointmentsController::class])
class CommunityPaybackControllerAdvice {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException) = ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse(status = HttpStatus.NOT_FOUND.value(), message = e.message))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(e: NoSuchElementException) = ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse(status = HttpStatus.NOT_FOUND.value(), message = e.message))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException) = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse(status = HttpStatus.BAD_REQUEST.value(), message = e.message))
}