package uk.gov.justice.digital.hmpps.sevice

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonSearchApi
import uk.gov.justice.digital.hmpps.integrations.prison.SearchRequest
import uk.gov.justice.digital.hmpps.sevice.model.PersonMatch

@Service
class Matcher(private val personRepository: PersonRepository, private val prisonSearchApi: PrisonSearchApi) {

    fun matchCrn(crn: String): PersonMatch? = personRepository.findByCrn(crn)?.let { person ->
        val searchRequest = person.asSearchRequest()
        val searchResults = prisonSearchApi.matchPerson(searchRequest).content
        val duplicateNoms = if (searchResults.size == 1) {
            personRepository.checkForDuplicateNoms(searchResults.first().prisonerNumber, person.id) > 0
        } else {
            null
        }
        PersonMatch(
            person,
            searchResults,
            duplicateNoms
        )
    }
}

private fun Person.asSearchRequest() =
    SearchRequest(
        nomsNumber?.trim() ?: pncNumber?.trim() ?: croNumber?.trim(),
        forename,
        surname,
        gender?.prisonGenderCode(),
        dateOfBirth
    )