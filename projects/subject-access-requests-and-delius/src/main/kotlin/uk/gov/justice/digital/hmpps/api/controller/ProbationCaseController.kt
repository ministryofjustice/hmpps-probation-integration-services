package uk.gov.justice.digital.hmpps.api.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.Person
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.entity.getPerson

@RestController
@PreAuthorize("hasRole('PROBATION_API__SUBJECT_ACCESS_REQUEST__DETAIL')")
class ProbationCaseController(private val personRepository: PersonRepository) {
    @GetMapping("/probation-case/{crn}")
    fun getPersonalDetails(@PathVariable crn: String) = with(personRepository.getPerson(crn)) {
        val middleName = listOfNotNull(secondName, thirdName).takeIf { it.isNotEmpty() }?.joinToString(" ")
        Person(Name(forename, middleName, surname))
    }
}
