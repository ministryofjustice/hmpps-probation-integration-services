package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class BasicDetails(
    val title: String?,
    val name: Name,
    val addresses: List<Address>,
    val prisonNumber: String?,
    val dateOfBirth: LocalDate
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String
)

data class Address(
    val id: Long,
    val status: String?,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val townCity: String?,
    val district: String?,
    val county: String?,
    val postcode: String?
)