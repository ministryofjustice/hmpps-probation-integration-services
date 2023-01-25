package uk.gov.justice.digital.hmpps.controller.personaldetails

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PersonController(val service: PersonalDetailsService) {
    @GetMapping(value = ["/case-data/{crn}/personal-details"])
    @PreAuthorize("hasRole('UPW_DETAILS')")
    fun personDetails(
        @PathVariable crn: String,
    ) = service.getPersonalDetails(crn)
}
