package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.overview.Rar

data class Requirement(
    val code: String,
//    val progressInHours: String?,
//    val progressInDays: String?,
    val description: String,
    val length: Long?,
    val notes: String?,
    val rar: Rar? = null
)
