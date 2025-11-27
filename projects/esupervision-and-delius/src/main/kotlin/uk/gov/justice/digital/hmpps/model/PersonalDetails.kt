package uk.gov.justice.digital.hmpps.model

data class PersonalDetails(
    val crn: String,
    val name: Name,
    val dateOfBirth: String
)