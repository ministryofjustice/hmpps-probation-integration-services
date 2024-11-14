package uk.gov.justice.digital.hmpps.api.model.appointment

data class Outcome(
    val id: Long,
    val code: String,
    val sensitive: Boolean,
    val notes: String?,
)


