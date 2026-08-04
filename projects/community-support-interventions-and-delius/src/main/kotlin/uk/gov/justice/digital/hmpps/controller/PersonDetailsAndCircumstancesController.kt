package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.PersonalDetailsService

@RestController
class PersonDetailsAndCircumstancesController(private val personalDetailsService: PersonalDetailsService) {
    @PreAuthorize("hasRole('PROBATION_API__COMMUNITY_SUPPORT_INTERVENTIONS__CASE_DETAIL')")
    @GetMapping(value = ["/case/{crn}"])
    fun getCase(@PathVariable crn: String) = personalDetailsService.getPersonDetails(crn)
}