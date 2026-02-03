package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.model.CaseDetails
import uk.gov.justice.digital.hmpps.model.LimitedAccessDetail
import uk.gov.justice.digital.hmpps.model.User
import uk.gov.justice.digital.hmpps.model.limitedAccessDetail
import uk.gov.justice.digital.hmpps.service.CaseDetailService
import uk.gov.justice.digital.hmpps.service.UserAccessService
import uk.gov.justice.digital.hmpps.service.UserService

@RestController
class ApiController(
    private val caseDetailService: CaseDetailService,
    private val userAccessService: UserAccessService,
    private val userService: UserService,
) {
    @GetMapping(value = ["/case/{crn}"])
    @PreAuthorize("hasRole('PROBATION_API__JITBIT__CASE_DETAIL')")
    @Operation(
        summary = "Retrieve case details for a given CRN",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Case details retrieved",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CaseDetails::class)
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Access denied due to exclusion or restriction",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = ErrorResponse::class,
                        example = """{"status": 403, "message": "Access has been denied as this case is a Limited Access case"}"""
                    )
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Case not found",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = ErrorResponse::class,
                        example = """{"status": 404, "message": "Person with CRN of A123456 not found"}"""
                    )
                )]
            )
        ]
    )
    fun getCaseDetails(@PathVariable crn: String): ResponseEntity<*> =
        if (checkAccess(crn).isLimitedAccess) ResponseEntity(
            ErrorResponse(
                status = HttpStatus.FORBIDDEN.value(),
                message = "Access has been denied as this case is a Limited Access case"
            ),
            HttpStatus.FORBIDDEN
        )
        else ResponseEntity.ok(caseDetailService.getCaseDetails(crn))

    @GetMapping(value = ["/user"])
    @PreAuthorize("hasRole('PROBATION_API__JITBIT__CASE_DETAIL')")
    @Operation(
        summary = "Check that a username exists",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "User exists",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = User::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(
                        implementation = ErrorResponse::class,
                        example = """{"status": 404, "message": "Username not found"}"""
                    )
                )]
            )
        ]
    )
    fun userExists(@RequestParam email: String): ResponseEntity<*> =
        if (userService.userExistsByEmail(email).exists)
            ResponseEntity.ok(User(true))
        else ResponseEntity(
            ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                message = "Username not found"
            ),
            HttpStatus.NOT_FOUND
        )

    @GetMapping(value = ["/case/{crn}/access"])
    @PreAuthorize("hasRole('PROBATION_API__JITBIT__CASE_DETAIL')")
    fun caseAccess(@PathVariable crn: String): LimitedAccessDetail = checkAccess(crn)

    private fun checkAccess(crn: String) =
        userAccessService.checkLimitedAccessFor(listOf(crn)).access.single { it.crn == crn }.limitedAccessDetail()
}
