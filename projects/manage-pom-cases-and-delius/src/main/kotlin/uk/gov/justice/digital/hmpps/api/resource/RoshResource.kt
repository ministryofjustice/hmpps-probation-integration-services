package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.services.RoshService

@RestController
class RoshResource(val roshService: RoshService) {
    @PreAuthorize("hasRole('PROBATION_API__MANAGE_POM_CASES__CASE_DETAIL')")
    @GetMapping(value = ["/case-records/{crn}/risks/rosh"])
    @Operation(
        summary = "Information on the ROSH level code",
        responses = [
            ApiResponse(responseCode = "200", description = "OK")
        ]
    )
    fun handle(
        @PathVariable("crn") crn: String
    ) = roshService.findByIdentifier(crn)
}