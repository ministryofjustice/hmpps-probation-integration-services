package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Address as AddressEntity

data class Address(
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val noFixedAbode: Boolean?,
)

fun AddressEntity.toAddress() =
    Address(
        buildingName = buildingName,
        addressNumber = addressNumber,
        streetName = streetName,
        town = town,
        district = district,
        county = county,
        postcode = postcode,
        noFixedAbode = noFixedAbode,
    )
