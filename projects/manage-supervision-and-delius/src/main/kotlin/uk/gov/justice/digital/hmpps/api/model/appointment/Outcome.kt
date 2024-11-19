package uk.gov.justice.digital.hmpps.api.model.appointment

data class Outcome(
    val id: Long,
    val recordedBy: String,
    val code: String,
    val attended: String,
    val sensitive: Boolean? = false,
    val notes: String? = null,
)


