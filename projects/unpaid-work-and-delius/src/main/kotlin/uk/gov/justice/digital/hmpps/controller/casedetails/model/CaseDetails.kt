package uk.gov.justice.digital.hmpps.controller.casedetails.model

import uk.gov.justice.digital.hmpps.controller.casedetails.entity.AliasEntity
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseEntity
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CasePersonalContactEntity
import uk.gov.justice.digital.hmpps.integrations.common.model.Address
import uk.gov.justice.digital.hmpps.integrations.common.model.Name
import uk.gov.justice.digital.hmpps.integrations.common.model.PersonalCircumstance
import uk.gov.justice.digital.hmpps.integrations.common.model.PersonalContact
import uk.gov.justice.digital.hmpps.integrations.common.model.Type
import java.time.LocalDate

data class CaseDetails(
    val crn: String,
    val name: Name,
    val dateOfBirth: LocalDate,
    val gender: String?,
    val genderIdentity: String?,
    val croNumber: String?,
    val pncNumber: String?,
    val aliases: List<Alias>? = listOf(),
    val emailAddress: String?,
    val phoneNumbers: List<PhoneNumber>? = listOf(),
    val mainAddress: Address?,
    val ethnicity: String?,
    val disabilities: List<Disability>? = listOf(),
    val provisions: List<Provision>?,
    val language: Language?,
    val personalCircumstances: List<PersonalCircumstance>? = listOf(),
    val personalContacts: List<PersonalContact>? = listOf(),
    val mappaRegistration: MappaRegistration?,
    val registerFlags: List<RegisterFlag>? = listOf(),
    val sentence: Sentence?,
)

data class Alias(
    val name: Name,
    val dateOfBirth: LocalDate,
)

data class PhoneNumber(
    val type: String,
    val number: String,
)

data class Disability(
    val type: Type,
    val condition: Type?,
    val notes: String?,
)

data class Provision(
    val type: Type,
    val category: Type?,
    val notes: String?,
)

data class Language(
    val requiresInterpreter: Boolean = false,
    val primaryLanguage: String = "",
)

data class MappaRegistration(
    val startDate: LocalDate,
    val level: Type,
    val category: Type,
)

data class RegisterFlag(
    val code: String,
    val description: String,
    val riskColour: String?,
)

data class Sentence(
    val startDate: LocalDate,
    val mainOffence: MainOffence,
)

data class MainOffence(
    val category: Type,
    val subCategory: Type,
)

fun CaseEntity.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)

fun AliasEntity.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)

fun CasePersonalContactEntity.name() = Name(forename, middleName, surname)
