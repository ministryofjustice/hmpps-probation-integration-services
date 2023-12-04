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
        return NomsUpdates(
            crns.map { crn ->
                val sentences = personRepository.findByCrn(crn)
                val person = sentences.firstOrNull()?.person
                var personMatch = getPersonMatch(crn, person, sentences.map { it.sentenceDate })

                // an extra check to see if the matched noms number is already used on another person. Update the match details if this is the case
                personMatch.matchedNomsNumber?.let { nomsNumber ->
                    val nomsPerson = personRepository.findByNomsNumberAndSoftDeletedIsFalse(nomsNumber)
                    nomsPerson?.let {
                        personMatch = personMatch.copy(
                            matchedNomsNumber = null,
                            matchDetail = MatchDetail(
                                "Person was matched to noms number but another person exists in delius with this noms number",
                                listOf(nomsNumber)
                            )
                        )
                    }
                }

                if (!trialOnly) {
                    updateNomsNumber(person, personMatch)
                }
                personMatch
            }
        )
    }

    private fun updateNomsNumber(
        person: Person?,
        personMatch: PersonMatch
    ) {
        person?.let { p ->
            personMatch.matchedNomsNumber?.let { nomsNo ->
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
                PersonMatch(crn, null, MatchDetail("Noms number already in Delius", listOf(person.nomsNumber!!)))
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
    var idMatch = true
    if (person.croNumber != null || person.pncNumber != null) {
        idMatch = pncNumber?.equals(person.pncNumber) == true || croNumber?.equals(person.croNumber) == true
    }
    return (
        idMatch &&
            sentenceDates.contains(sentenceStartDate) &&
            dateOfBirth == person.dateOfBirth &&
            firstName.equals(person.forename, true) &&
            lastName.equals(person.surname, true)
        )
}

fun Person.asSearchRequest() =
    SearchRequest(pncNumber?.trim() ?: croNumber?.trim(), forename, surname, gender?.prisonGenderCode(), dateOfBirth)
