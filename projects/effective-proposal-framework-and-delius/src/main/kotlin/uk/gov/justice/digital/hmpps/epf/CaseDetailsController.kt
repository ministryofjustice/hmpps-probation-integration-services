package uk.gov.justice.digital.hmpps.epf

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CaseDetailsController(private val caseDetailsService: CaseDetailsService) {
    @PreAuthorize("hasRole('PROBATION_API__EPF__CASE_DETAIL')")
    @GetMapping(value = ["/case-details/{crn}/{eventNumber}"])
    @Operation(
        summary = "Probation case information for the Effective Proposals Framework service",
        description = """<p>Accepts the probation identifier (CRN) and Delius Event number
            and returns a data structure giving background information on the probation case
            for use in the Effective Proposals Framework system. The information is used to
            reduce the need for the EPF user to re-key information already held in Delius.</p>
            <p>Requires `PROBATION_API__EPF__CASE_DETAIL`.</p>
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(
                responseCode = "404",
                description = """A case with the provided CRN does not exist in Delius.
                    Note: this could be the result of a merge or a deletion.
            """
            )
        ]
    )
    fun handle(
        @PathVariable("crn") crn: String,
        @PathVariable("eventNumber") eventNumber: Int
    ) = caseDetailsService.caseDetails(crn, eventNumber)
}
