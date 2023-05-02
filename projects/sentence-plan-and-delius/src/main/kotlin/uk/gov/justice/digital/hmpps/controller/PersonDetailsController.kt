package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.PersonDetailsService

@RestController
class PersonDetailsController(
    private val personDetailsService: PersonDetailsService
) {
    @PreAuthorize("hasRole('ROLE_EXAMPLE')")
    @GetMapping(value = ["person-details/{crn}"])
    fun getPersonDetails(
        @PathVariable("crn") crn: String
    ) = personDetailsService.getPersonalDetails(crn)

}
