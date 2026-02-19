package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseScheduleService
import uk.gov.justice.digital.hmpps.service.CaseSummaryService

@RestController
@RequestMapping("/case")
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class CaseController(
    private val caseScheduleService: CaseScheduleService,
    private val caseSummaryService: CaseSummaryService,
) {
    @GetMapping("/{crn}/event/{eventNumber}/appointments/schedule")
    fun getSchedule(
        @PathVariable crn: String,
        @PathVariable eventNumber: String
    ) = caseScheduleService.getSchedule(crn, eventNumber)

    @GetMapping("/{crn}/summary")
    fun getSummary(
        @PathVariable crn: String
    ) = caseSummaryService.getSummaryForCase(crn)
}
