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

data class OfficeAddress(
    val id: Long,
    val status: String?,
    val officeDescription: String?,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val townCity: String?,
    val district: String?,
    val county: String?,
    val postcode: String?
)