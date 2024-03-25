package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.client.*
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.entity.getByCrn
import uk.gov.justice.digital.hmpps.messaging.Notifier
import uk.gov.justice.digital.hmpps.model.MatchResult
import uk.gov.justice.digital.hmpps.model.MatchResult.*
import uk.gov.justice.digital.hmpps.model.MergeResult
import uk.gov.justice.digital.hmpps.model.PrisonIdentifiers
import java.time.LocalDate

@Service
class ProbationMatchingService(
    private val prisonApiClient: PrisonApiClient,
    private val probationSearchClient: ProbationSearchClient,
    private val personRepository: PersonRepository,
    private val matchWriter: MatchWriter,
    private val notifier: Notifier,
    private val objectMapper: ObjectMapper,
) {
    fun matchAndUpdateIdentifiers(nomsNumber: String, dryRun: Boolean): MatchResult {
        val matchResult = findMatchingProbationRecord(nomsNumber)
        if (!dryRun && matchResult is Success) {
            with(matchResult) {
                val changes = matchWriter.update(prisonIdentifiers, person, custody)
                if (changes) notifier.identifierAdded(person.crn, prisonIdentifiers)
            }
        }
        return matchResult
    }

    fun replaceIdentifiers(oldNomsNumber: String, newNomsNumber: String, dryRun: Boolean): MergeResult {
        personRepository.findAllByNomsNumber(newNomsNumber).joinToString { it.crn }.takeIf { it.isNotEmpty() }?.let {
            return MergeResult.Ignored("NOMS number $newNomsNumber is already assigned to $it")
        }

        val existing = personRepository.findAllByNomsNumber(oldNomsNumber)
        if (existing.isEmpty()) {
            return MergeResult.Ignored("No records found for NOMS number $oldNomsNumber")
        }
        if (!dryRun) {
            existing.forEach {
                val changes = matchWriter.update(PrisonIdentifiers(newNomsNumber), it)
                if (changes) notifier.identifierUpdated(it.crn, newNomsNumber, oldNomsNumber)
            }
        }
        return MergeResult.Success(
            "Replaced NOMS numbers for ${existing.size} record${if (existing.size == 1) "" else "s"}", mapOf(
                "existingNomsNumber" to oldNomsNumber,
                "updatedNomsNumber" to newNomsNumber,
                "matches" to objectMapper.writeValueAsString(existing.map { mapOf("crn" to it.crn) })
            )
        )
    }

    private fun findMatchingProbationRecord(nomsNumber: String): MatchResult {
        // Get prisoner details
        val booking = prisonApiClient.getBooking(nomsNumber).takeIf { it.activeFlag }
            ?: return Ignored("No active booking")
        val sentenceDate = prisonApiClient.getSentenceTerms(booking.bookingId).latestPrimarySentenceStartDate()
            ?: return Ignored("No sentence start date")
        val identifiers = PrisonIdentifiers(nomsNumber, booking.bookingNo)
        val prisoner = prisonApiClient.getPrisoners(nomsNumber).single()

        // Get matching probation records
        val matchResponse = probationSearchClient.match(ProbationMatchRequest(prisoner))

        // Compare sentence dates
        val matchingCustodies = matchResponse.matches.crns()
            .flatMap { personRepository.getByCrn(it).custodiesWithSentenceDateCloseTo(sentenceDate) }
        val matchingCustody = matchingCustodies.singleOrNull()
        val matchingPerson = matchingCustodies.map { it.disposal.event.person }.distinctBy { it.crn }.singleOrNull()
            ?: return NoMatch(
                "No single match found in probation system",
                booking.telemetry() + matchResponse.telemetry() + mapOf("sentenceDateInNomis" to sentenceDate)
            )

        return Success(
            identifiers, matchingPerson, matchingCustody, booking.telemetry() + matchResponse.telemetry() + mapOf(
                "existingNomsNumber" to matchingPerson.nomsNumber,
                "matchedNomsNumber" to identifiers.prisonerNumber,
                "nomsNumberChanged" to (identifiers.prisonerNumber != matchingPerson.nomsNumber),
                "existingBookingNumber" to matchingCustody?.prisonerNumber,
                "matchedBookingNumber" to identifiers.bookingNumber,
                "bookingNumberChanged" to (matchingCustody != null && identifiers.bookingNumber != matchingCustody.prisonerNumber),
                "custody" to matchingCustody?.id,
                "sentenceDateInDelius" to matchingCustody?.disposal?.startDate,
                "sentenceDateInNomis" to sentenceDate,
                "totalCustodialEvents" to matchingPerson.custodies().size,
                "matchingCustodialEvents" to matchingCustodies.size,
            )
        )
    }

    private fun List<SentenceSummary>.latestPrimarySentenceStartDate(): LocalDate? =
        filter { it.startDate != null && it.consecutiveTo == null }.maxOfOrNull { it.startDate!! }

    private fun List<OffenderMatch>.crns() = map { it.offender.otherIds.crn }

    private fun Booking.telemetry() = mapOf(
        "nomsNumber" to offenderNo,
        "bookingNo" to bookingNo,
    )

    private fun ProbationMatchResponse.telemetry() = mapOf(
        "matchedBy" to matchedBy,
        "potentialMatches" to objectMapper.writeValueAsString(matches.crns().map { mapOf("crn" to it) })
    )
}