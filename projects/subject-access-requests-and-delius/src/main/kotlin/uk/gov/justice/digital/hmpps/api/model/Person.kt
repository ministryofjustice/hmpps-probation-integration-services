package uk.gov.justice.digital.hmpps.api.model

data class Person(
    val name: Name
)

data class Name(
    val forename: String,
    val middleName: String? = null,
    val surname: String
)