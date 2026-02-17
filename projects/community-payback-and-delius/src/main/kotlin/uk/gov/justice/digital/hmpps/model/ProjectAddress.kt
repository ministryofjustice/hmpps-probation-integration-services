package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.person.Address

data class ProjectAddress(
    val streetName: String?,
    val buildingName: String?,
    val addressNumber: String?,
    val townCity: String?,
    val county: String?,
    val postCode: String?
) {
    constructor(entity: Address) : this(
        streetName = entity.streetName,
        buildingName = entity.buildingName,
        addressNumber = entity.addressNumber,
        townCity = entity.town,
        county = entity.county,
        postCode = entity.postcode
    )
}