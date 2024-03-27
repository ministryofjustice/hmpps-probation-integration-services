package uk.gov.justice.digital.hmpps.api.model.sentence

data class Requirement(
    val description: String,
    val length: Long,
    val progress: Long,
    val notes: String
)
