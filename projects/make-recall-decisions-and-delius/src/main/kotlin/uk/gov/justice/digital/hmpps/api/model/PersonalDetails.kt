package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Address
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Person
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

fun Person.identifiers() = PersonalDetails.Identifiers(pncNumber, croNumber, nomsNumber, bookingNumber = mostRecentPrisonerNumber)
fun Address.toAddress() = PersonalDetails.Address(
    buildingName = buildingName,
    addressNumber = addressNumber,
    streetName = streetName,
    town = town,
    county = county,
    postcode = postcode,
    noFixedAbode = noFixedAbode
)
