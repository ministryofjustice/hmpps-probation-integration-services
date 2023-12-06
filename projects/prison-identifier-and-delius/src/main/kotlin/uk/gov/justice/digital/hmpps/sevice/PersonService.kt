package uk.gov.justice.digital.hmpps.sevice

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.SentencedPerson
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonSearchAPI
import uk.gov.justice.digital.hmpps.sevice.model.MatchDetail
import uk.gov.justice.digital.hmpps.sevice.model.MatchReason
import uk.gov.justice.digital.hmpps.sevice.model.NomsUpdates
import uk.gov.justice.digital.hmpps.sevice.model.PersonMatch
import uk.gov.justice.digital.hmpps.sevice.model.PrisonSearchResult
import uk.gov.justice.digital.hmpps.sevice.model.SearchRequest
import java.lang.Math.abs
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

@Service
class PersonService(
    val personRepository: PersonRepository,
    val custodyRepository: CustodyRepository,
    val prisonSearchAPI: PrisonSearchAPI
) {
    fun populateNomsNumber(crns: List<String>, trialOnly: Boolean): NomsUpdates {
        return NomsUpdates(
            crns.map { crn ->
                val sentences = personRepository.findByCrn(crn)
                val person = sentences.firstOrNull()?.person
                val personMatch = getPersonMatch(crn, person, sentences.map { it.sentenceDate })
                    .withValidation(sentences)

                if (!trialOnly && personMatch.matchDetail != null) {
                    sentences.matching(personMatch.matchDetail.sentenceDate).first().also {
                        updateNomsNumber(person!!, personMatch)
                        updateBookingRef(it.custody, personMatch.matchDetail.bookingRef)
                    }
                }
                personMatch
            }
        )
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
                PersonMatch(crn, null, MatchReason("Person not found via prison search api"))
            }
            // found one exact match
            searchResults.size == 1 -> {
                val match = searchResults.first().matches(person, sentenceDates)
                val reason = when (match) {
                    is MatchResult.Match -> MatchReason("Found a single match in prison search api")
                    else -> MatchReason("Found a single match in prison search api but this did not match the Delius person")
                }
                PersonMatch(
                    crn,
                    match.matchDetail(),
                    reason
                )
            }
            // found multiple matches attempt to find an exact match
            else -> {
                findPersonMatch(person, sentenceDates, searchResults, crn)
            }
        }
    }

    private fun PersonMatch.withValidation(sentences: List<SentencedPerson>): PersonMatch =
        // an extra check to see if the matched noms number is already used on another person. Update the match details if this is the case
        matchDetail?.let { matchDetail ->
            val nomsPerson = personRepository.findByNomsNumberAndSoftDeletedIsFalse(matchDetail.nomsNumber)
            if (nomsPerson == null) {
                if (sentences.matching(matchDetail.sentenceDate).size == 1) {
                    this
                } else {
                    PersonMatch(
                        crn,
                        matchDetail = null,
                        matchReason = MatchReason(
                            "Unable to match as there are multiple matching custody records with matching sentence dates",
                            listOfNotNull(matchDetail)
                        )
                    )
                }
            } else {
                PersonMatch(
                    crn,
                    matchDetail = null,
                    matchReason = MatchReason(
                        "Person was matched to noms number but another person exists in delius with this noms number",
                        listOfNotNull(matchDetail)
                    )
                )
            }
        } ?: this

    private fun updateBookingRef(custody: Custody, bookingRef: String) {
        custody.bookingRef = bookingRef
        custodyRepository.save(custody)
    }

    private fun updateNomsNumber(
        person: Person,
        personMatch: PersonMatch
    ) {
        person.nomsNumber = personMatch.matchDetail!!.nomsNumber
        personRepository.save(person)
    }

    private fun findPersonMatch(
        person: Person,
        sentenceDates: List<LocalDate>,
        searchResults: List<PrisonSearchResult>,
        crn: String
    ): PersonMatch {
        val criteriaMatch = applyMatchingCriteria(person, sentenceDates, searchResults)
        val matchedNomsData = criteriaMatch.filterIsInstance<MatchResult.Match>()

        return if (matchedNomsData.size == 1) {
            PersonMatch(
                crn,
                matchedNomsData.first().matchDetail(),
                MatchReason(
                    "Found a single match in prison search api and matching criteria."
                )
            )
        } else {
            PersonMatch(
                crn,
                null,
                MatchReason(
                    "Unable to find a unique match using matching criteria.",
                    criteriaMatch.mapNotNull { it.matchDetail() }
                )
            )
        }
    }

    private fun checkDelius(person: Person?, crn: String) =
        when {
            person == null -> {
                PersonMatch(crn, null, MatchReason("CRN not found in Delius"))
            }

            person.nomsNumber != null -> {
                PersonMatch(
                    crn,
                    null,
                    MatchReason("This person already has a noms number in Delius")
                )
            }

            else -> {
                null
            }
        }

    private fun applyMatchingCriteria(
        person: Person,
        sentenceDates: List<LocalDate>,
        searchResults: List<PrisonSearchResult>
    ): List<MatchResult> =
        searchResults.map {
            it.matches(person, sentenceDates)
        }
}

private fun List<SentencedPerson>.matching(sentenceDate: LocalDate): List<SentencedPerson> =
    filter { it.sentenceDate.closeTo(sentenceDate) }

private fun LocalDate.closeTo(date: LocalDate, days: Int = 7): Boolean = abs(DAYS.between(this, date)) <= days
private fun MatchResult.matchDetail() = when (this) {
    is MatchResult.Match -> MatchDetail(
        prisonSearchResult.prisonerNumber,
        prisonSearchResult.bookingNumber,
        prisonSearchResult.sentenceStartDate!!
    )

    else -> null
}

private fun PrisonSearchResult.matches(person: Person, sentenceDates: List<LocalDate>): MatchResult =
    if ((sentenceStartDate != null && sentenceDates.any { it.closeTo(sentenceStartDate) }) &&
        (
            pncNumber?.equals(person.pncNumber) == true || croNumber?.equals(person.croNumber) == true || person.matches(
                firstName,
                lastName,
                dateOfBirth
            )
            )
    ) {
        MatchResult.Match(this)
    } else {
        MatchResult.NoMatch
    }

private fun Person.matches(firstName: String, lastName: String, dateOfBirth: LocalDate) =
    dateOfBirth == this.dateOfBirth &&
        firstName.equals(forename, true) &&
        lastName.equals(surname, true)

private fun Person.asSearchRequest() =
    SearchRequest(pncNumber?.trim() ?: croNumber?.trim(), forename, surname, gender?.prisonGenderCode(), dateOfBirth)

sealed interface MatchResult {
    data object NoMatch : MatchResult
    data class Match(val prisonSearchResult: PrisonSearchResult) : MatchResult
}
