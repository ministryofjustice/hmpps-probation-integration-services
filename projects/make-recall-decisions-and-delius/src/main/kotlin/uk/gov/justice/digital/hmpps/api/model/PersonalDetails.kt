package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class PersonalDetails(
    val name: Name,
    val identifiers: Identifiers,
    val dateOfBirth: LocalDate,
    val gender: String,
    val ethnicity: String?,
    val primaryLanguage: String?,
    val mainAddress: Address?
) {
    data class Name(
        val forename: String,
        val middleName: String?,
        val surname: String
    )
    data class Identifiers(
        val pncNumber: String?,
        val croNumber: String?,
        val nomsNumber: String?,
        val bookingNumber: String?
    )
    data class Address(
        val buildingName: String?,
        val addressNumber: String?,
        val streetName: String?,
        val town: String?,
        val county: String?,
        val postcode: String?,
        val noFixedAbode: Boolean?
    )
}
