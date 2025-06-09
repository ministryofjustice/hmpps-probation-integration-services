package uk.gov.justice.digital.hmpps.api.model.appointment

data class OfficeLocationRequest(
    val provideCode: String,
    val teamCode: String
)

data class StaffLocationRequest(
    val teamCode: String,
    val locationCode: String
)