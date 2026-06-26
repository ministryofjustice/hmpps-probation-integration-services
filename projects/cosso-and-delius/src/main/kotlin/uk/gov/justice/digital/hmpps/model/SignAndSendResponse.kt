package uk.gov.justice.digital.hmpps.model

data class SignAndSendResponse(
    val userDetails: UserDetails,
    val responsibleOfficer: ResponsibleOfficerResponse
)

data class UserDetails(
    val name: Name?,
)

data class ResponsibleOfficerResponse(
    val title: String?,
    val name: Name?,
    val telephoneNumber: String?,
    val emailAddress: String?,
    val addresses: List<OfficeAddress>
)
