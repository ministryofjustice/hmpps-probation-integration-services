package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.PersonSummary

data class SentenceOverview(
    val personSummary: PersonSummary,
    val sentenceSummaryList: List<SentenceSummary> = emptyList(),
    val sentence: Sentence? = null
)

data class SentenceSummary(
    val eventNumber: String,
    val description: String
)

data class MinimalSentenceOverview(
    val personSummary: PersonSummary,
    val sentences: List<MinimalSentence> = emptyList(),
)