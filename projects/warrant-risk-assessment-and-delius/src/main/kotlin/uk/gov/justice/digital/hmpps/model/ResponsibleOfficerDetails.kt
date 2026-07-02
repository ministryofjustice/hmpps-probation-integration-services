package uk.gov.justice.digital.hmpps.model

import com.fasterxml.jackson.annotation.JsonInclude

data class ResponsibleOfficerDetails(
    val name: Name,
    val telephoneNumber: String?,
    val probationArea: CodeAndDescription,
    val replyAddress: OfficeAddress?,
)

data class CodeAndDescription(
    val code: String,
    val description: String,
)

data class OfficeAddress(
    val id: Long?,
    val status: String?,
    val officeDescription: String?,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val townCity: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,
)
