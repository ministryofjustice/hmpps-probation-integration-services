package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseViewService

@RestController
@RequestMapping("/allocation-demand")
class CaseViewResource(val service: CaseViewService) {
    @PreAuthorize("hasAnyRole('ROLE_ALLOCATION_CONTEXT','PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = "Detailed information on the probation case",
        description = """Detailed information on the probation case identified
            by the CRN and event number provided in the request. Returns information
            on the person on probation, offences, sentence, requirements and case
            documents held in Delius. Used to support the 'Case View' screen of the
            HMPPS Workforce service which provides a detailed overview of the case
            when allocating to a probation practitioner
        """
    )
    @GetMapping("/{crn}/{eventNumber}/case-view")
    fun caseView(@PathVariable crn: String, @PathVariable eventNumber: String) = service.caseView(crn, eventNumber)
}
