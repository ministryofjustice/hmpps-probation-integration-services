package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.delius.service.OffenderService

@RestController
@RequestMapping("probation-case/{crn}")
class ProbationRecordResource(private val offenderService: OffenderService) {

    @PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
    @GetMapping
    fun getOffenderDetailSummary(
        @PathVariable crn: String
    ) = offenderService.getOffenderDetail(crn)
}
