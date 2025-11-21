package uk.gov.justice.digital.hmpps.api.model.personalDetails

data class PersonContactInformation(
    val crn: String,
    val telephoneNumber: String?,
    val mobileNumber: String?,
    val email: String?
)