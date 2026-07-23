package uk.gov.justice.digital.hmpps.integrations.crds

data class OperativeSentenceEnvelope(
    val bookingId: Long,
    val containsAnSDSPlusSentence: Boolean? = null,
    val sentenceEnvelopeLengthInDays: Long
)