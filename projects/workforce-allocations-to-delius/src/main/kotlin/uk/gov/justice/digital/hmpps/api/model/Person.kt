package uk.gov.justice.digital.hmpps.api.model

data class Person(
    val crn: String,
    val name: Name,
    val type: CaseType,
)
