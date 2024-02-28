package uk.gov.justice.digital.hmpps.api.model.overview

data class Rar(
    val completed: Int,
    val scheduled: Int,
    val totalDays: Int = completed + scheduled
)