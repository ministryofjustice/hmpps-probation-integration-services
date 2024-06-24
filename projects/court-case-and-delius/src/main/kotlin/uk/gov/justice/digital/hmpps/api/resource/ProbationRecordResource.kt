package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.integrations.delius.service.OffenderManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.service.OffenderService

@RestController
@RequestMapping("probation-case/{crn}")
class ProbationRecordResource(
    private val offenderService: OffenderService,
    private val offenderManagerService: OffenderManagerService
) {

    @PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @GetMapping
    fun getOffenderDetailSummary(
        @PathVariable crn: String
    ) = offenderService.getOffenderDetailSummary(crn)

    @PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @GetMapping("/all")
    fun getOffenderDetail(
        @PathVariable crn: String
    ) = offenderService.getOffenderDetail(crn)

    @PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @GetMapping("/allOffenderManagers")
    fun getAllOffenderManagers(
        @PathVariable crn: String,
        @RequestParam(defaultValue = "false", required = false) includeProbationAreaTeams: Boolean
    ) = offenderManagerService.getAllOffenderManagersForCrn(crn, includeProbationAreaTeams)
}
