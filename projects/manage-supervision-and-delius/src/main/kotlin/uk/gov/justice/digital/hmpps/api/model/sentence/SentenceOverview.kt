package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.PersonSummary

data class SentenceOverview(
    val personSummary: PersonSummary,
    val sentences: List<Sentence>,
    val probationHistory: ProbationHistory,
)
