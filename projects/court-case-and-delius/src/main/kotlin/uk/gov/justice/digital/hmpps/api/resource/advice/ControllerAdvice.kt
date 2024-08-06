package uk.gov.justice.digital.hmpps.api.resource.advice

import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.api.resource.ConvictionResource
import uk.gov.justice.digital.hmpps.api.resource.ProbationRecordResource
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@RestControllerAdvice(basePackageClasses = [ProbationRecordResource::class, ConvictionResource::class])
class CommunityApiControllerAdvice {
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException) = ResponseEntity
        .status(NOT_FOUND)
        .body(ErrorResponse(status = NOT_FOUND.value(), developerMessage = e.message))

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException) = ResponseEntity
        .status(FORBIDDEN)
        .body(ErrorResponse(status = FORBIDDEN.value(), developerMessage = e.message))
}
