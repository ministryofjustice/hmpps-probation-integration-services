package uk.gov.justice.digital.hmpps.model

data class SignAndSendResponse(
    val userDetails: Name?,
    val responsibleOfficer: TitleAndName?,
    val telephoneNumber: String?,
    val emailAddress: String?,
    val addresses: List<OfficeAddress>
)

data class TitleAndName(
    val title: String?,
    val name: Name?,
)