package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.time.LocalDateTime

data class PersonalDetails(
    val name: Name,
    val preferredName: String?,
    val dateOfBirth: LocalDate,
    val lastUpdatedAt: LocalDateTime? = null,
    val mainAddress: Address?,
    val telephoneNumber: String?,
    val mobileNumber: String?,
    val emailAddress: String?,
    val emergencyContacts: List<PersonalContact>,
    val practitioner: Manager,
)