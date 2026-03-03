package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class ContactDetails(
    val crn: String,
    val name: Name,
    val mobile: String?,
    val email: String?,
    val events: List<Event>,
    val practitioner: Practitioner,
)

data class Name(val forename: String, val surname: String)

data class CodedDescription(val code: String, val description: String)

data class Event(
    val number: Int,
    val mainOffence: CodedDescription,
    val sentence: Sentence?,
) {
    data class Sentence(
        val date: LocalDate,
        val description: String,
    )
}

data class Practitioner(
    val code: String,
    val name: Name,
    val localAdminUnit: CodedDescription,
    val probationDeliveryUnit: CodedDescription,
    val provider: CodedDescription,
    val email: String?,
)
