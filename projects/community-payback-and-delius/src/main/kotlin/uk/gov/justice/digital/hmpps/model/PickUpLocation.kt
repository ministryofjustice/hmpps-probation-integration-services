package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation

data class PickUpLocation(
    val code: Code,
    val description: String,
    val streetName: String?,
    val buildingName: String?,
    val addressNumber: String?,
    val townCity: String?,
    val county: String?,
    val postCode: String?
)

fun OfficeLocation.toPickUpLocation() = PickUpLocation(
    Code(this.code),
    this.description,
    this.streetName,
    this.buildingName,
    this.buildingNumber,
    this.town,
    this.county,
    this.postcode
)