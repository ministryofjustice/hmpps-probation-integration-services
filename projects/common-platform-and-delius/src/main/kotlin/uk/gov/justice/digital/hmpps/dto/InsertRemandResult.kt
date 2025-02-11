package uk.gov.justice.digital.hmpps.dto

data class InsertRemandResult (
    val insertPersonResult: InsertPersonResult,
    val insertEventResult: InsertEventResult
)
