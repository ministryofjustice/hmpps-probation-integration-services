package uk.gov.justice.digital.hmpps.model

data class ResponsibleOfficerDetails(
    val name: Name,
    val emailAddress: String?,
    val telephoneNumber: String?,
    val probationArea: CodeAndDescription,
    val replyAddresses: List<OfficeAddress>,
)

data class CodeAndDescription(
    val code: String,
    val description: String,
)

data class OfficeAddress(
    val id: Long?,
    val status: String?,
    val officeDescription: String?,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val townCity: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,
)