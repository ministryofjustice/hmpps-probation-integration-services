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
import uk.gov.justice.digital.hmpps.model.ResponsibleOfficerDetails
import uk.gov.justice.digital.hmpps.service.ResponsibleOfficerService

@RestController
class ResponsibleOfficerController(
    private val responsibleOfficerService: ResponsibleOfficerService,
) {
    @GetMapping("/responsible-officer/{crn}")
    @PreAuthorize("hasRole('PROBATION_API__WARRANT_RISK_ASSESSMENT')")
    @Operation(
        summary = "Retrieve responsible officer details for a person",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Responsible officer details retrieved",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ResponsibleOfficerDetails::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Person or responsible officer not found",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    fun getResponsibleOfficerDetails(@PathVariable crn: String): ResponsibleOfficerDetails =
        responsibleOfficerService.getResponsibleOfficerDetails(crn)
}
