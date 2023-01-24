package uk.gov.justice.digital.hmpps.controller.personaldetails.model

import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonalContactEntity

data class PersonalDetails(

    val crn: String,
    val personalCircumstances: List<PersonalCircumstance>,
    val personalContacts: List<PersonalContact>,
)

data class PersonalContact(
    val relationship: String,
    val name: Name,
    val telephoneNumber: String?,
    val mobileNumber: String?,
    val address: Address?,
)

data class Address(
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
)

data class PersonalCircumstance(
    val type: Type,
    val subType: Type?,
    val notes: String,
    val evidenced: Boolean,
)

data class Type(
    val code: String,
    val description: String,
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String,
)

fun PersonalContactEntity.name() = Name(forename, middleName, surname)
