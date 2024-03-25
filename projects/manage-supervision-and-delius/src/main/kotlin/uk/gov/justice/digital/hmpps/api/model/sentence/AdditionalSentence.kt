package uk.gov.justice.digital.hmpps.api.model.sentence

data class AdditionalSentence(
    val length: Long?,
    val amount: Long?,
    val notes: String?,
    val description: String,
)
