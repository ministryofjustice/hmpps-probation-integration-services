package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import java.time.ZonedDateTime

data class APDocument(
    val id: String?,
    val level: String,
    val eventNumber: String?,
    val filename: String,
    val typeCode: String,
    val typeDescription: String,
    val dateSaved: ZonedDateTime?,
    val dateCreated: ZonedDateTime?,
    val description: String?
)

data class RelatedTo(
    val type: RelatedType,
    val name: String = "",
    val event: DocumentEvent? = null
) {
    val description: String = type.description()
}

data class DocumentEvent(
    val eventType: EventType,
    val eventNumber: String,
    val mainOffence: String
)

enum class EventType {
    CURRENT, PREVIOUS
}

enum class RelatedType(private val displayName: String = "") {
    ADDRESS_ASSESSMENT,
    APPROVED_PREMISES_REFERRAL,
    ASSESSMENT,
    CASE_ALLOCATION,
    CONTACT,
    COURT_REPORT,
    CPSPACK("Crown Prosecution Service case pack"),
    DRUGS_TEST("Drug Test"),
    PRECONS("PNC previous convictions"),
    EVENT,
    EQUALITY("Equality Monitoring"),
    INSTITUTIONAL_REPORT,
    NSI("Non Statutory Intervention"),
    PERSON,
    OFFENDER_ADDRESS,
    PERSONAL_CONTACT,
    PERSONAL_CIRCUMSTANCE,
    REFERRAL,
    REGISTRATION,
    UPW_APPOINTMENT("Unpaid Work Appointment");

    fun description(): String =
        displayName.ifEmpty { name.split("_").joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) } }
}
