package uk.gov.justice.digital.hmpps.service

import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.*

@Transactional
@Service
class MatchService(private val personRepository: PersonRepository) {
    fun findMatches(request: MatchRequest): MatchResponse {
        val all = personRepository.findAll(request.allSpecified())
        if (all.isNotEmpty()) return MatchResponse(all.matches(), MatchedBy.ALL_SUPPLIED)
        val allAlias = personRepository.findAll(request.allAlias())
        if (allAlias.isNotEmpty()) return MatchResponse(allAlias.matches(), MatchedBy.ALL_SUPPLIED_ALIAS)
        val pnc = request.pncNumber?.let { personRepository.findAll(matchesPnc(it)) } ?: emptyList()
        if (pnc.isNotEmpty()) return MatchResponse(pnc.matches(), MatchedBy.EXTERNAL_KEY)
        val name = personRepository.findAll(request.name())
        if (name.isNotEmpty()) return MatchResponse(name.matches(), MatchedBy.NAME)
        val partialName = personRepository.findAll(request.partialName())
        if (partialName.isNotEmpty()) return MatchResponse(partialName.matches(), MatchedBy.PARTIAL_NAME)
        val lenient = personRepository.findAll(request.lenient())
        if (lenient.isNotEmpty()) return MatchResponse(lenient.matches(), MatchedBy.PARTIAL_NAME_DOB_LENIENT)
        return MatchResponse(emptyList(), MatchedBy.NOTHING)
    }

    private fun MatchRequest.allSpecified(): Specification<Person> = listOfNotNull(
        hasActiveEventAndCommunityManager(activeSentence),
        matchesPerson(surname, firstName, dateOfBirth),
        pncNumber?.let { matchesPnc(it) }
    ).reduce { a, b -> a.and(b) }

    private fun MatchRequest.allAlias(): Specification<Person> = listOfNotNull(
        hasActiveEventAndCommunityManager(activeSentence),
        matchesAlias(surname, firstName, dateOfBirth),
        pncNumber?.let { matchesPnc(it) }
    ).reduce { a, b -> a.and(b) }

    private fun MatchRequest.name(): Specification<Person> =
        hasActiveEventAndCommunityManager(activeSentence).and(
            matchesPerson(surname, firstName, dateOfBirth)
                .or(matchesAlias(surname, firstName, dateOfBirth))
        )

    private fun MatchRequest.partialName(): Specification<Person> =
        hasActiveEventAndCommunityManager(activeSentence).and(matchesPerson(surname, null, dateOfBirth))

    private fun MatchRequest.lenient(): Specification<Person> =
        hasActiveEventAndCommunityManager(activeSentence).and(matchesLeniently(surname, firstName, dateOfBirth))

    private fun Person.statusDetail() =
        personRepository.statusOf(crn)?.matchStatusDetail() ?: MatchProbationStatus.NO_RECORD

    private fun List<Person>.matches() = map { it.asMatch(it.statusDetail()) }
}

private fun Person.asMatch(status: MatchProbationStatus): Match =
    Match(
        MatchedPerson(
            forename,
            surname,
            dateOfBirth,
            Ids(crn, nomsNumber, pnc, croNumber),
            status,
            offenderAliases.map {
                uk.gov.justice.digital.hmpps.api.model.OffenderAlias(
                    id = it.aliasID.toString(),
                    dateOfBirth = it.dateOfBirth,
                    firstName = it.firstName,
                    middleNames = listOfNotNull(it.secondName, it.thirdName).takeIf { n -> n.isNotEmpty() },
                    surname = it.surname,
                    gender = it.gender.description
                )
            }
        )
    )

fun SentenceCounts.matchStatusDetail() = MatchProbationStatus(
    status,
    terminationDate,
    breachCount > 0,
    preSentenceCount > 0,
    awaitingPsrCount > 0
)