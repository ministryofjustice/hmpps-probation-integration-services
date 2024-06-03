package uk.gov.justice.digital.hmpps.advice

import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@RestControllerAdvice(basePackages = ["uk.gov.justice.digital.hmpps"])
class ControllerAdvice {
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException) = ResponseEntity
        .status(NOT_FOUND)
        .body(ErrorResponse(status = NOT_FOUND.value(), message = e.message))

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(e: ConflictException) = ResponseEntity
        .status(CONFLICT)
        .body(ErrorResponse(status = CONFLICT.value(), message = e.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException) = ResponseEntity
        .badRequest()
        .body(
            ErrorResponse(
                status = BAD_REQUEST.value(),
                message = "Validation failure",
                fields = e.bindingResult.fieldErrors.map { FieldError(it.code, it.defaultMessage, it.field) }
            )
        )

    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequest(e: InvalidRequestException) = ResponseEntity
        .status(BAD_REQUEST)
        .body(ErrorResponse(status = BAD_REQUEST.value(), message = e.message))
}
