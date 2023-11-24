package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository

@RestController
class CrnResource(private val personRepository: PersonRepository) {
    @GetMapping("/probation-cases")
    @PreAuthorize("hasRole('TIER_DETAILS')")
    fun findAllActiveCrns() = personRepository.findAllCrns()
}
