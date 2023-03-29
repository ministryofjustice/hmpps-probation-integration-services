package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Address
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Person
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Team
import java.time.LocalDate

data class PersonalDetailsOverview(
    val name: Name,
    val identifiers: Identifiers,
    val dateOfBirth: LocalDate,
    val gender: String,
    val ethnicity: String?,
    val primaryLanguage: String?
) {
    data class Identifiers(
        val pncNumber: String?,
        val croNumber: String?,
        val nomsNumber: String?,
        val bookingNumber: String?
    )
}

data class PersonalDetails(
    val personalDetails: PersonalDetailsOverview,
    val mainAddress: Address?,
    val communityManager: Manager?
) {
    data class Address(
        val buildingName: String?,
        val addressNumber: String?,
        val streetName: String?,
        val district: String?,
        val town: String?,
        val county: String?,
        val postcode: String?,
        val noFixedAbode: Boolean?
    )
    data class Manager(
        val staffCode: String,
        val name: Name,
        val provider: Provider,
        val team: Team
    ) {
        data class Provider(
            val code: String,
            val name: String
        )
        data class Team(
            val code: String,
            val name: String,
            val localAdminUnit: String,
            val telephone: String?,
            val email: String?
        )
    }
}

fun Address.toAddress() = PersonalDetails.Address(
    buildingName = buildingName,
    addressNumber = addressNumber,
    streetName = streetName,
    town = town,
    district = district,
    county = county,
    postcode = postcode,
    noFixedAbode = noFixedAbode
)
fun Team.toTeam() = PersonalDetails.Manager.Team(code, description, localAdminUnit = district.description, telephone, emailAddress)
fun Provider.toProvider() = PersonalDetails.Manager.Provider(code, description)
fun PersonManager.toManager() = PersonalDetails.Manager(
    staffCode = staff.code,
    name = staff.name(),
    team = team.toTeam(),
    provider = provider.toProvider()
)

fun Person.identifiers() = PersonalDetailsOverview.Identifiers(pncNumber, croNumber, nomsNumber, bookingNumber = mostRecentPrisonerNumber)
