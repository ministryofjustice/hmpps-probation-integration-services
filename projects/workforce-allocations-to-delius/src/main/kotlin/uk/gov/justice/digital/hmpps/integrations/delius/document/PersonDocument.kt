package uk.gov.justice.digital.hmpps.integrations.delius.document

import java.time.ZonedDateTime

data class PersonDocument(
    val id: String?,
    val name: String,
    val relatedTo: RelatedTo,
    val dateSaved: ZonedDateTime?,
    val dateCreated: ZonedDateTime?,
    val sensitive: Boolean,
)

data class RelatedTo(
    val type: RelatedType,
    val name: String = "",
    val event: DocumentEvent? = null,
) {
    val description: String = type.description()
}

data class DocumentEvent(
    val eventType: EventType,
    val eventNumber: String,
    val mainOffence: String,
)

enum class EventType {
    CURRENT,
    PREVIOUS,
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
    UPW_APPOINTMENT("Unpaid Work Appointment"),
    ;

    fun description(): String =
        displayName.ifEmpty { name.split("_").joinToString(" ") { it.lowercase().replaceFirstChar(Char::titlecase) } }
}
