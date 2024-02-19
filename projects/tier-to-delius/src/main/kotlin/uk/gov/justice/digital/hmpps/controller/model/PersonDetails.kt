package uk.gov.justice.digital.hmpps.controller.model

data class PersonDetails(
    val crn: String,
    val name: Name
)

data class Name(
    val forenames: String,
    val surname: String
)
