package uk.gov.justice.digital.hmpps.model

data class ResponsibleOfficerDetails(
    val name: Name,
    val emailAddress: String?,
    val telephoneNumber: String?,
    val probationArea: CodeAndDescription,
    val replyAddress: OfficeAddress?,
)

data class OfficeAddress(
    val id: Long?,
    val officeDescription: String?,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val townCity: String?,
    val district: String?,
    val county: String?,
    val postcode: String?
) {
    constructor() : this(null, null, null, null, null, null, null, null, null)
}