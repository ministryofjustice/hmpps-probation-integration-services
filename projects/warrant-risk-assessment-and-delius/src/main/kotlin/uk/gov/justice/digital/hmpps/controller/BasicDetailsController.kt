package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.model.BasicDetails
import uk.gov.justice.digital.hmpps.service.BasicDetailsService

@RestController
class BasicDetailsController(private val basicDetailsService: BasicDetailsService) {

    @GetMapping("/basic-details/{crn}")
    @PreAuthorize("hasRole('PROBATION_API__WARRANT_RISK_ASSESSMENT__BASIC_DETAILS')")
    @Operation(
        summary = "Retrieve basic offender details for the WRA form",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Basic details retrieved",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BasicDetails::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Person not found for the given CRN",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    fun getBasicDetails(@PathVariable crn: String): BasicDetails =
        basicDetailsService.getBasicDetails(crn)
}
