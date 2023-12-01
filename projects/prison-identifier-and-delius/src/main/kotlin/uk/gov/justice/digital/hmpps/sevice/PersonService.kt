package uk.gov.justice.digital.hmpps.sevice

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonSearchAPI
import uk.gov.justice.digital.hmpps.sevice.model.MatchDetail
import uk.gov.justice.digital.hmpps.sevice.model.NomsUpdates
import uk.gov.justice.digital.hmpps.sevice.model.PersonMatch
import uk.gov.justice.digital.hmpps.sevice.model.PrisonSearchResult
import uk.gov.justice.digital.hmpps.sevice.model.SearchRequest
import java.time.LocalDate

@Service
class PersonService(
    val personRepository: PersonRepository,
    val prisonSearchAPI: PrisonSearchAPI
) {
    fun populateNomsNumber(crns: List<String>, trialOnly: Boolean): NomsUpdates {
        val personMatches = arrayListOf<PersonMatch>()
        crns.forEach { crn ->
            val sentences = personRepository.findByCrn(crn)
            val person = sentences.firstOrNull()?.person
            val personMatch = getPersonMatch(crn, person, sentences.map { it.sentenceDate })

            if (!trialOnly) {
                // TODO Add a check in here to see if another offender has this nomsnumber if so update the personMatch to say so and do not update the offender
                updateNomsNumber(person, personMatch)
            }
            personMatches.add(personMatch)
        }
        return NomsUpdates(personMatches)
    }

    private fun updateNomsNumber(
        person: Person?,
        personMatch: PersonMatch
    ) {
        person?.let { p ->
            personMatch.nomsNumber?.let { nomsNo ->
                p.nomsNumber = nomsNo
                personRepository.save(p)
            }
        }
    }

    private fun getPersonMatch(crn: String, person: Person?, sentenceDates: List<LocalDate>): PersonMatch {
        // check the person exists in delius and that they don't already have a noms number
        val personMatch = checkDelius(person, crn)
        if (personMatch != null) return personMatch

        // attempt to find the person in nomis
        val searchResults = prisonSearchAPI.matchPerson(person!!.asSearchRequest()).content
        return when {
            // not found
            searchResults.isEmpty() -> {
                PersonMatch(crn, null, MatchDetail("Person not found via prison search api", listOf()))
            }
            // found one exact match
            searchResults.size == 1 && searchResults.first().matches(person, sentenceDates) -> {
                PersonMatch(
                    crn,
                    searchResults.first().prisonerNumber,
                    MatchDetail("Found a single match in prison search api", listOf())
                )
            }
            // found multiple matches attempt to find an exact match
            else -> {
                findPersonMatch(person, sentenceDates, searchResults, crn)
            }
        }
    }

    private fun findPersonMatch(
        person: Person,
        sentenceDates: List<LocalDate>,
        searchResults: List<PrisonSearchResult>,
        crn: String
    ): PersonMatch {
        val matchedNomsNumbers = applyMatchingCriteria(person, sentenceDates, searchResults)

        return if (matchedNomsNumbers.size == 1) {
            PersonMatch(
                crn,
                matchedNomsNumbers.first(),
                MatchDetail(
                    "Found a single match in prison search api and matching criteria.",
                    listOf()
                )
            )
        } else {
            PersonMatch(
                crn,
                null,
                MatchDetail(
                    "Unable to find a unique match using matching criteria.",
                    matchedNomsNumbers
                )
            )
        }
    }

    private fun checkDelius(person: Person?, crn: String) =
        when {
            person == null -> {
                PersonMatch(crn, null, MatchDetail("CRN not found in Delius", listOf()))
            }

            person.nomsNumber != null -> {
                PersonMatch(crn, person.nomsNumber, MatchDetail("Noms number already in Delius", listOf()))
            }

            else -> {
                null
            }
        }

    private fun applyMatchingCriteria(
        person: Person,
        sentenceDates: List<LocalDate>,
        searchResults: List<PrisonSearchResult>
    ): List<String> =
        searchResults.mapNotNull {
            if (it.matches(person, sentenceDates)) {
                it.prisonerNumber
            } else {
                null
            }
        }
}

private fun PrisonSearchResult.matches(person: Person, sentenceDates: List<LocalDate>): Boolean {
    return if (pncNumber?.equals(person.pncNumber) == true || croNumber?.equals(person.croNumber) == true) {
        true
    } else {
        sentenceDates.contains(sentenceStartDate)
    }
}

fun Person.asSearchRequest() =
    SearchRequest(pncNumber?.trim() ?: croNumber?.trim(), forename, surname, gender?.prisonGenderCode(), dateOfBirth)
