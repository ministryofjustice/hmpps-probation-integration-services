package uk.gov.justice.digital.hmpps.api.model.sentence

data class UserOfficeLocation(
    val name: Name,
    val locations: List<LocationDetails>
)

data class Name(
    val forename: String,
    val middleName: String? = null,
    val surname: String
)

data class LocationDetails(
    val id: Long,
    val description: String,
    val address: Address
)

data class Address(
    val buildingNumber: String? = null,
    val streetName: String? = null,
    val town: String? = null,
    val county: String? = null,
    val postcode: String? = null
)