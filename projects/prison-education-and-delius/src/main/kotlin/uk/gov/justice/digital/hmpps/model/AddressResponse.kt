package uk.gov.justice.digital.hmpps.model

data class AddressResponse(
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val noFixedAbode: Boolean?
)