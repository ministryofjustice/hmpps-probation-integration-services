package uk.gov.justice.digital.hmpps.model

data class SignAndSendResponse(
    val name: Name,
    val telephoneNumber: String?,
    val emailAddress: String?,
    val addresses: List<OfficeAddress>
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