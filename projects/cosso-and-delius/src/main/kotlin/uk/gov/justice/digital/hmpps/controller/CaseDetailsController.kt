package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.PersonDetails
import uk.gov.justice.digital.hmpps.service.PersonService

@RestController
class CaseDetailsController(val personService: PersonService) {
    @PreAuthorize("hasRole('PROBATION_API__COSSO__CASE_DETAILS')")
    @GetMapping(value = ["/basic-details/{crn}"])
    fun getBasicDetails(@PathVariable("crn") crn: String): PersonDetails =
        personService.getBasicDetails(crn)
}