package uk.gov.justice.digital.hmpps.api.resource.advice

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.api.resource.ConvictionResource
import uk.gov.justice.digital.hmpps.api.resource.DocumentResource
import uk.gov.justice.digital.hmpps.api.resource.ProbationRecordResource
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@RestControllerAdvice(basePackageClasses = [ProbationRecordResource::class, ConvictionResource::class, DocumentResource::class])
class CommunityApiControllerAdvice {

    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequest(e: InvalidRequestException) = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse(status = HttpStatus.BAD_REQUEST.value(), developerMessage = e.message))

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException) = ResponseEntity
        .status(NOT_FOUND)
        .body(ErrorResponse(status = NOT_FOUND.value(), developerMessage = e.message))

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException) = ResponseEntity
        .status(FORBIDDEN)
        .body(ErrorResponse(status = FORBIDDEN.value(), developerMessage = e.message))
}
