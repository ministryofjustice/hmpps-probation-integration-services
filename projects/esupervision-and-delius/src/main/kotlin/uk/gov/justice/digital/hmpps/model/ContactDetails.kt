package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class ContactDetails(
    val crn: String,
    val name: Name,
    val dateOfBirth: LocalDate,
    val dateOfDeath: LocalDate?,
    val mobile: String?,
    val email: String?,
    val events: List<Event>,
    val practitioner: Practitioner,
    val contactSuspended: Boolean,
    val activeShpoOrSopo: Boolean,
)

data class Name(val forename: String, val surname: String)

data class CodedDescription(val code: String, val description: String)

data class Event(
    val number: Int,
    val mainOffence: CodedDescription,
    val sentence: Sentence?,
    val youthSentence: Boolean,
    val licenceConditions: List<LicenceCondition>,
) {
    data class Sentence(
        val date: LocalDate,
        val description: String,
        val expectedEndDate: LocalDate?,
        val length: Long?,
        val lengthUnit: String?,
    )
}

data class LicenceCondition(
    val startDate: LocalDate,
    val mainCategory: CodedDescription,
    val subCategory: CodedDescription?,
    val notes: String?,
)

data class Practitioner(
    val code: String,
    val name: Name,
    val localAdminUnit: CodedDescription,
    val probationDeliveryUnit: CodedDescription,
    val provider: CodedDescription,
    val email: String?,
    val unallocated: Boolean,
    val username: String?,
)

data class UpdateContactDetails(
    val mobileNumber: String?,
    val emailAddress: String?,
)

data class AlertCount(
    val count: Long,
)