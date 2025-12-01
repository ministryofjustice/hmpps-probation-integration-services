package uk.gov.justice.digital.hmpps.advice

import jakarta.validation.ConstraintViolationException
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@RestControllerAdvice(basePackages = ["uk.gov.justice.digital.hmpps"])
class ControllerAdvice {
    @ExceptionHandler(NotFoundException::class)
    fun handle(e: NotFoundException) = ResponseEntity
        .status(NOT_FOUND)
        .body(ErrorResponse(status = NOT_FOUND.value(), message = e.message))

    @ExceptionHandler(ConflictException::class)
    fun handle(e: ConflictException) = ResponseEntity
        .status(CONFLICT)
        .body(ErrorResponse(status = CONFLICT.value(), message = e.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(e: MethodArgumentNotValidException) = ResponseEntity
        .badRequest()
        .body(
            ErrorResponse(
                status = BAD_REQUEST.value(),
                message = "Validation failure",
                fields = e.bindingResult.fieldErrors.map { FieldError(it.code, it.defaultMessage, it.field) }
            )
        )

    @ExceptionHandler(InvalidRequestException::class)
    fun handle(e: InvalidRequestException) = ResponseEntity
        .status(BAD_REQUEST)
        .body(ErrorResponse(status = BAD_REQUEST.value(), message = e.message))

    @ExceptionHandler(ConstraintViolationException::class)
    fun handle(e: ConstraintViolationException) = ResponseEntity
        .status(BAD_REQUEST)
        .body(ErrorResponse(status = BAD_REQUEST.value(), message = e.message))

    @ExceptionHandler(AccessDeniedException::class)
    fun handle(e: AccessDeniedException) = ResponseEntity
        .status(FORBIDDEN)
        .body(ErrorResponse(status = FORBIDDEN.value(), message = e.message))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handle(e: IllegalArgumentException) = ResponseEntity
        .status(BAD_REQUEST)
        .body(ErrorResponse(status = BAD_REQUEST.value(), message = e.message))
}

@RestControllerAdvice(basePackages = ["uk.gov.justice.digital.hmpps"])
@ConditionalOnClass(name = ["org.springframework.dao.DataAccessException"])
class JpaControllerAdvice {
    @ExceptionHandler(DataAccessException::class)
    fun handle(e: DataAccessException) = ResponseEntity
        .status(CONFLICT)
        .body(ErrorResponse(status = CONFLICT.value(), message = e.message))
}