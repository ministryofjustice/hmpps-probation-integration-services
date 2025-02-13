package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.DetailsService
import uk.gov.justice.digital.hmpps.model.BasicDetails

@RestController
class BreachNoticeController(private val details: DetailsService) {
    @PreAuthorize("hasRole('PROBATION_API__BREACH_NOTICE__CASE_DETAIL')")
    @GetMapping(value = ["/basic-details/{crn}/{username}"])
    fun handle(@PathVariable crn: String, @PathVariable username: String): BasicDetails =
        details.basicDetails(crn, username)
}
