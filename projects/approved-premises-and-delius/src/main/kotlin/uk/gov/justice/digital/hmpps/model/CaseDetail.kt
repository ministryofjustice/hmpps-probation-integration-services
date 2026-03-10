package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class CaseSummaries(val cases: List<CaseSummary>)

data class CaseSummary(
    val crn: String,
    val nomsId: String?,
    val pnc: String?,
    val name: Name,
    val dateOfBirth: LocalDate,
    val gender: String?,
    val profile: Profile?,
    val manager: Manager,
    val currentExclusion: Boolean,
    val currentRestriction: Boolean
)

data class CaseDetail(
    val case: CaseSummary,
    val offences: List<Offence>,
    val registrations: List<Registration>,
    val mappaDetail: MappaDetail?,
    val careLeaver: Boolean,
    val veteran: Boolean,
    val sentences: List<Sentence>,
    val personalContacts: List<PersonalContact>? = listOf(),
)

data class PersonalContact(
    val relationship: String,
    val relationshipType: Type,
    val name: ContactName,
    val telephoneNumber: String?,
    val mobileNumber: String?,
    val address: Address?
)

data class Type(
    val code: String,
    val description: String
)

data class ContactName(
    val forename: String,
    val middleName: String?,
    val surname: String
)

data class Address(
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?
)

data class Name(val forename: String, val surname: String, val middleNames: List<String>)

data class Profile(
    val ethnicity: String?,
    val genderIdentity: String?,
    val nationality: String?,
    val religion: String?
)

data class Manager(val team: Team)

data class Team(
    val code: String, val name: String, val ldu: Ldu,
    val borough: Borough?,
    val startDate: LocalDate,
    val endDate: LocalDate?
)

data class Borough(val code: String, val description: String)

data class Ldu(val code: String, val name: String)

data class MappaDetail(
    val level: Int?,
    val levelDescription: String?,
    val category: Int?,
    val categoryDescription: String?,
    val startDate: LocalDate,
    val lastUpdated: ZonedDateTime
)

data class Offence(
    val id: String,
    val code: String,
    val description: String,
    val mainCategoryDescription: String,
    val subCategoryDescription: String,
    val date: LocalDate?,
    val main: Boolean,
    val eventId: Long,
    val eventNumber: String
)

data class Registration(val code: String, val description: String, val startDate: LocalDate)

data class Sentence(
    val typeDescription: String?,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val eventNumber: String? = null,
)