package uk.gov.justice.digital.hmpps.integrations.crds

data class OperativeSentenceEnvelope(
    val sentenceEnvelopeLengthInDays: Long,
    val containsAnSDSPlusSentence: Boolean = false,
)
