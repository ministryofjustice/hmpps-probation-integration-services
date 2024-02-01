package uk.gov.justice.digital.hmpps.controller.casedetails

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CaseDetailsController(val service: CaseDetailsService) {

    @PreAuthorize("hasAnyRole('UPW_DETAILS','PROBATION_API__UPW__CASE_DETAIL')")
    @Operation(
        summary = "Detailed information on the probation case",
        description = """Full details of the probation case for the purposes
            of creating a new unpaid work assessment. The service will return
            case information for the CRN and Event Number supplied in the
            request. Only active personal contacts and personal circumstances
            are included
        """
    )
    @GetMapping(value = ["/case-data/{crn}/{eventId}"])
    fun personDetails(
        @PathVariable crn: String,
        @PathVariable eventId: Long
    ) = service.getCaseDetails(crn, eventId)
}
