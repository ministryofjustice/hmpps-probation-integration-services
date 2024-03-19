package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.client.PrisonerSearchResult
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.service.DateMatcher
import uk.gov.justice.digital.hmpps.service.withinDays

data class PrisonerMatches(
    val person: Person,
    val responses: List<PrisonerSearchResult>
) {

    private fun nameMatchType(prisoner: PrisonerSearchResult): ComponentMatch.MatchType =
        when {
            prisoner.firstName.equals(person.forename, true) &&
                prisoner.lastName.equals(person.surname, true) -> ComponentMatch.MatchType.MATCH

            !prisoner.firstName.equals(person.forename, true) &&
                !prisoner.lastName.equals(person.surname, true) -> ComponentMatch.MatchType.INCONCLUSIVE

            else -> ComponentMatch.MatchType.PARTIAL
        }

    private fun nameMatch(prisoner: PrisonerSearchResult) = ComponentMatch.Name(nameMatchType(prisoner))

    private fun dobMatch(prisoner: PrisonerSearchResult) = ComponentMatch.DateOfBirth(
        when (prisoner.dateOfBirth) {
            person.dateOfBirth -> ComponentMatch.MatchType.MATCH
            in DateMatcher.variations(person.dateOfBirth) -> ComponentMatch.MatchType.PARTIAL
            else -> ComponentMatch.MatchType.INCONCLUSIVE
        }
    )

    private fun identifierMatch(prisoner: PrisonerSearchResult): ComponentMatch.Identifier? = listOf(
        prisoner.prisonerNumber to person.nomsNumber,
        prisoner.pncNumber to person.pncNumber,
        prisoner.croNumber to person.croNumber
    ).filter { !exclusiveField(it.first, it.second) }
        .take(1)
        .map {
            ComponentMatch.Identifier(if (it.first == it.second) ComponentMatch.MatchType.MATCH else ComponentMatch.MatchType.INCONCLUSIVE)
        }.firstOrNull()

    private fun exclusiveField(first: String?, second: String?): Boolean =
        (first == null && second != null) || (first != null && second == null)

    private fun sentenceDateMatch(prisoner: PrisonerSearchResult) = ComponentMatch.SentenceDate(
        when {
            person.isSentenced() && (prisoner.sentenceStartDate != null && person.sentenceDates()
                .any { it.withinDays(prisoner.sentenceStartDate) }) -> ComponentMatch.MatchType.MATCH

            !person.isSentenced() && prisoner.sentenceStartDate == null -> ComponentMatch.MatchType.PARTIAL
            else -> ComponentMatch.MatchType.INCONCLUSIVE
        }
    )

    private fun componentMatches(prisoner: PrisonerSearchResult): List<ComponentMatch> =
        when (val idMatch = identifierMatch(prisoner)) {
            is ComponentMatch.Identifier -> listOf(idMatch)
            else -> listOfNotNull(
                nameMatch(prisoner),
                dobMatch(prisoner),
                sentenceDateMatch(prisoner)
            )
        }

    val potentialMatches = responses.map { PotentialMatch(it, componentMatches(it)) }

    val matches =
        potentialMatches.filter { potential -> potential.matches.all { it.type == ComponentMatch.MatchType.MATCH } }

    val match: PrisonerSearchResult? = if (matches.size == 1) matches.first().prisoner else null
}

sealed interface ComponentMatch {
    val type: MatchType

    fun name() = this::class.simpleName

    data class Name(override val type: MatchType) : ComponentMatch
    data class DateOfBirth(override val type: MatchType) : ComponentMatch
    data class Identifier(override val type: MatchType) : ComponentMatch
    data class SentenceDate(override val type: MatchType) : ComponentMatch
    enum class MatchType {
        MATCH, PARTIAL, INCONCLUSIVE
    }
}

data class PotentialMatch(val prisoner: PrisonerSearchResult, val matches: List<ComponentMatch>)