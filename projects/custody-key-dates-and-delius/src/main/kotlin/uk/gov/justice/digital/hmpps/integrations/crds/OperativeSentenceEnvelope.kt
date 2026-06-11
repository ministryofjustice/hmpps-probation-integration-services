package uk.gov.justice.digital.hmpps.integrations.crds

import java.time.LocalDate

data class OperativeSentenceEnvelope(
    val sentenceEnvelopeLengthInDays: Long? = null,
    val earliestSentenceStartDate: LocalDate? = null,
    val isPostRecallSentenceEnvelope: Boolean? = null,
    val containsAnSDSPlusSentence: Boolean = false,
    val sentenceEnvelopeSource: String? = null
)
