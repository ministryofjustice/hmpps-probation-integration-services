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
import uk.gov.justice.digital.hmpps.model.MappaInformation
import uk.gov.justice.digital.hmpps.service.MappaInformationService

@RestController
class MappaInformationController(
    private val mappaInformationService: MappaInformationService,
) {
    @GetMapping("/mappa-information/{crn}")
    @PreAuthorize("hasRole('PROBATION_API__WARRANT_RISK_ASSESSMENT__CASE_DETAIL')")
    @Operation(
        summary = "Retrieve MAPPA information for a person",
        description = "Returns the latest open MAPPA registration for the given CRN",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "MAPPA information retrieved",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = MappaInformation::class)
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
    fun getMappaInformation(@PathVariable crn: String): MappaInformation =
        mappaInformationService.getMappaInformation(crn)
}