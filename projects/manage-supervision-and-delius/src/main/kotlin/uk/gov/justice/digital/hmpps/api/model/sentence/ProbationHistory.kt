package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import java.time.LocalDate

data class History(
    val personSummary: PersonSummary,
    val sentenceSummaryList: List<SentenceSummary> = emptyList(),
    val probationHistory: ProbationHistory
)
data class ProbationHistory(
    val numberOfTerminatedEvents: Int,
    val dateOfMostRecentTerminatedEvent: LocalDate?,
    val numberOfTerminatedEventBreaches: Int,
    val numberOfProfessionalContacts: Long
)
