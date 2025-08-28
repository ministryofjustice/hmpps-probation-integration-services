package uk.gov.justice.digital.hmpps.api.model.appointment

data class Outcome(
    val id: Long,
    val outcomeRecorded: Boolean,
    val notes: String?,
    val sensitive: Boolean = false,
)