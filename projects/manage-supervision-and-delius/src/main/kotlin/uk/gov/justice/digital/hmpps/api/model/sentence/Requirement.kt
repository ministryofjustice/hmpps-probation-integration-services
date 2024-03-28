package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.overview.Rar

data class Requirement(
    val description: String?,
    val codeDescription: String?,
    val length: Long?,
    val notes: String?,
    val rar: Rar? = null
)
