package uk.gov.justice.digital.hmpps.api.model.overview

data class Rar(
    val completed: Int,
    val nsiCompleted: Int,
    val scheduled: Int,
    val totalDays: Int = nsiCompleted + completed + scheduled
)