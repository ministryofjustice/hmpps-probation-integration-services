package uk.gov.justice.digital.hmpps.model

data class ContactDetails(
    val crn: String, val name: Name,
    val mobile: String?, val email: String?
)

data class Name(val forename: String, val surname: String)
