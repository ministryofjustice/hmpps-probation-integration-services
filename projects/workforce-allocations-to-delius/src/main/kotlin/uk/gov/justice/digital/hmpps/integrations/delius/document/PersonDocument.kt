package uk.gov.justice.digital.hmpps.integrations.delius.document

import java.time.ZonedDateTime

data class PersonDocument(
    val id: Long,
    val name: String,
    val relatedTo: RelatedTo,
    val dateSaved: ZonedDateTime,
    val sensitive: Boolean
)

data class RelatedTo(
    val type: RelatedType,
    val name: String = "",
    val event: DocumentEvent? = null
)

data class DocumentEvent(
    val eventType: EventType,
    val eventNumber: String,
    val mainOffence: String
)

enum class EventType {
    CURRENT, PREVIOUS
}

enum class RelatedType {
    ADDRESS_ASSESSMENT,
    APPROVED_PREMISES_REFERRAL,
    ASSESSMENT,
    CASE_ALLOCATION,
    CONTACT,
    COURT_REPORT,
    DRUGS_TEST,
    EQUALITY,
    EVENT,
    INSTITUTIONAL_REPORT,
    NSI,
    OFFENDER,
    OFFENDER_ADDRESS,
    PERSONAL_CONTACT,
    PERSONAL_CIRCUMSTANCE,
    REFERRAL,
    REGISTRATION,
    UPW_APPOINTMENT
}
