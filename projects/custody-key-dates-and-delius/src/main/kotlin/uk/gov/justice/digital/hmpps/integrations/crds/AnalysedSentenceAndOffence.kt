package uk.gov.justice.digital.hmpps.integrations.crds

data class AnalysedSentenceAndOffence(
    val isSDSPlus: Boolean,
    val effectiveSentenceLengthDays: Long
)