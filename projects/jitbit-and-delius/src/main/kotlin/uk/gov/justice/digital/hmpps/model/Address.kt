package uk.gov.justice.digital.hmpps.model

data class Address(
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val townCity: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,
    val noFixedAbode: Boolean,
)