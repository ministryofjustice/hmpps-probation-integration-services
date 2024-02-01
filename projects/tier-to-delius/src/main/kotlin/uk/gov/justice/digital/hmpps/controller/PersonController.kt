package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.controller.model.Name
import uk.gov.justice.digital.hmpps.controller.model.PersonDetails
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository

@RestController
class PersonController(private val personRepository: PersonRepository) {
    @GetMapping(value = ["/person/{crn}"])
    @PreAuthorize("hasAnyRole('TIER_DETAILS','PROBATION_API__TIER__CASE_DETAIL')")
    fun personDetails(@PathVariable crn: String) = personRepository.findByCrnAndSoftDeletedIsFalse(crn)?.let {
        PersonDetails(
            crn = it.crn,
            name = Name(
                forenames = listOfNotNull(it.forename, it.secondName, it.thirdName).joinToString(" "),
                surname = it.surname
            )
        )
    }
}
