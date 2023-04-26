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
    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @Operation(
        summary = "Detailed information on the probation case",
        description = """Detailed information on the probation case identified
            by the CRN and Event Number provided in the request. Returns information
            on the person on probation, offences, sentence, requirements and case
            documents held in Delius. Used to support the 'Case View' screen of the
            HMPPS Workforce service
        """
    )
    @GetMapping("/{crn}/{eventNumber}/case-view")
    fun caseView(@PathVariable crn: String, @PathVariable eventNumber: String) = service.caseView(crn, eventNumber)
}
