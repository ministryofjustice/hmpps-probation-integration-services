package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.CaseDetails
import uk.gov.justice.digital.hmpps.service.CaseDetailsService

@RestController
@PreAuthorize("hasRole('PROBATION_API__HMPPS_API__CASE_DETAIL')")
@Tag(name = "Case Details", description = "Requires PROBATION_API__HMPPS_API__CASE_DETAIL")
class CaseDetailsController(
    private val caseDetailsService: CaseDetailsService
) {
    @GetMapping(value = ["/case/{crn}/supervisions"])
    @Operation(summary = "Get a list of supervisions (called “events” in Delius) for a probation case, by CRN")
    fun supervisions(@PathVariable("crn") crn: String) = caseDetailsService.getSupervisions(crn)

    @GetMapping(value = ["/case/{crn}/addresses"])
    fun addresses(@PathVariable crn: String) = caseDetailsService.getAddresses(crn)

    @GetMapping(value = ["/case-details/{crn}/{eventNumber}"])
    @Operation(summary = "Probation case information for the Effective Proposals Framework service")
    fun getCaseDetails(
        @PathVariable("crn") crn: String,
        @PathVariable("eventNumber") eventNumber: Int
    ): CaseDetails = caseDetailsService.getCaseDetails(crn, eventNumber)
}
