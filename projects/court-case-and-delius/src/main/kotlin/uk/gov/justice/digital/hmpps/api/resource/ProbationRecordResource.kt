package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.OffenderDetail
import uk.gov.justice.digital.hmpps.api.model.OffenderDetailSummary
import uk.gov.justice.digital.hmpps.integrations.delius.service.OffenderManagerService
import uk.gov.justice.digital.hmpps.integrations.delius.service.OffenderService
import uk.gov.justice.digital.hmpps.service.LaoService

@RestController
@RequestMapping("probation-case/{crn}")
class ProbationRecordResource(
    private val offenderService: OffenderService,
    private val offenderManagerService: OffenderManagerService,
    private val laoService: LaoService
) {

    @PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @GetMapping
    fun getOffenderDetailSummary(
        @PathVariable crn: String
    ): OffenderDetailSummary {
        laoService.checkLao(crn)
        return offenderService.getOffenderDetailSummary(crn)
    }

    @PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @GetMapping("/all")
    fun getOffenderDetail(
        @PathVariable crn: String
    ): OffenderDetail {
        laoService.checkLao(crn)
        return offenderService.getOffenderDetail(crn)
    }

    @PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @GetMapping("/allOffenderManagers")
    fun getAllOffenderManagers(
        @PathVariable crn: String,
        @RequestParam(defaultValue = "false", required = false) includeProbationAreaTeams: Boolean
    ) = offenderManagerService.getAllOffenderManagersForCrn(crn, includeProbationAreaTeams)
}
