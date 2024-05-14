package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.client.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.client.PrisonerSearchRequest
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.messaging.Notifier
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.model.MatchResult.NoMatch
import uk.gov.justice.digital.hmpps.model.MatchResult.Success

@Service
class PrisonMatchingService(
    private val personRepository: PersonRepository,
    private val prisonerSearchClient: PrisonerSearchClient,
    private val matchWriter: MatchWriter,
    private val notifier: Notifier,
    private val objectMapper: ObjectMapper,
) {
    fun matchAndUpdateIdentifiers(crn: String, dryRun: Boolean = false): MatchResult {
        val matchResult = findMatchingPrisonRecord(crn)
        if (!dryRun && matchResult is Success) {
            with(matchResult) {
                val changes = matchWriter.update(prisonIdentifiers, person, custody)
                if (changes) notifier.identifierAdded(person.crn, prisonIdentifiers)
            }
        }
        return matchResult
    }

    private fun findMatchingPrisonRecord(crn: String): MatchResult {
        // Get person on probation details
        val person = personRepository.findByCrn(crn)
            ?: return MatchResult.Ignored("CRN soft deleted", mapOf("crn" to crn))

        // Get matching prisoner records
        val searchResults = prisonerSearchClient.globalSearch(person.asSearchRequest()).content
        val prisonerMatches = PrisonerMatches(person, searchResults)
        val matchedPrisoner = prisonerMatches.match
            ?: return NoMatch("No single match found in prison system", prisonerMatches.telemetry())

        // Compare sentence dates
        val identifiers = PrisonIdentifiers(matchedPrisoner.prisonerNumber, matchedPrisoner.bookingNumber)
        val matchingCustodies = matchedPrisoner.sentenceStartDate
            ?.let { person.custodiesWithSentenceDateCloseTo(it) } ?: emptyList()
        val matchingCustody = matchingCustodies.singleOrNull()

        return Success(
            identifiers, person, matchingCustody, prisonerMatches.telemetry() + mapOf(
                "existingNomsNumber" to person.nomsNumber,
                "matchedNomsNumber" to identifiers.prisonerNumber,
                "nomsNumberChanged" to (identifiers.prisonerNumber != person.nomsNumber),
                "existingBookingNumber" to matchingCustody?.prisonerNumber,
                "matchedBookingNumber" to identifiers.bookingNumber,
                "bookingNumberChanged" to (matchingCustody != null && identifiers.bookingNumber != matchingCustody.prisonerNumber),
                "custody" to matchingCustody?.id,
                "sentenceDateInDelius" to matchingCustody?.disposal?.startDate,
                "sentenceDateInNomis" to matchedPrisoner.sentenceStartDate,
                "totalCustodialEvents" to person.custodies().size,
                "matchingCustodialEvents" to matchingCustodies.size,
            )
        )
    }

    private fun Person.asSearchRequest() = PrisonerSearchRequest(
        nomsNumber?.trim() ?: pncNumber?.trim() ?: croNumber?.trim(),
        forename,
        surname,
        gender?.prisonGenderCode(),
        dateOfBirth
    )

    fun PrisonerMatches.telemetry() = mapOf(
        "crn" to person.crn,
        "potentialMatches" to potentialMatches.telemetry()
    )

    fun List<PotentialMatch>.telemetry(): String = objectMapper.writeValueAsString(map { potentialMatch ->
        mapOf("nomsNumber" to potentialMatch.prisoner.prisonerNumber) +
            potentialMatch.matches
                .filter { it.type != ComponentMatch.MatchType.MATCH }
                .associate { component -> component.name() to component.type }
    })
}