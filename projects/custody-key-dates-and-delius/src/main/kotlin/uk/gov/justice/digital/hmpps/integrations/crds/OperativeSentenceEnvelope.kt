package uk.gov.justice.digital.hmpps.integrations.crds

data class OperativeSentenceEnvelope(
    val sentenceEnvelopeLengthInDays: Long? = null,
    val containsAnSDSPlusSentence: Boolean = false,
)
