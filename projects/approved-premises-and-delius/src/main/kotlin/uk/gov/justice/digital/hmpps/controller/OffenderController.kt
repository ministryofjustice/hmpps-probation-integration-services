package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.OffenderDetail
import uk.gov.justice.digital.hmpps.service.OffenderService

@RestController
@RequestMapping("offenders")
@PreAuthorize("hasRole('PROBATION_API__APPROVED_PREMISES__CASE_DETAIL')")
class OffenderController(
    private val offenderService: OffenderService
) {
    @GetMapping("/crn/{crn}/all")
    fun getOffenderDetail(@PathVariable crn: String): OffenderDetail = offenderService.getOffenderDetail(crn)
}