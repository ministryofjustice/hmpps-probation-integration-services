package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.api.model.ContactHistory.Contact.DocumentReference
import uk.gov.justice.digital.hmpps.api.model.ContactHistory.Contact.Type
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Contact
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

data class ContactHistory(
    val personalDetails: PersonalDetailsOverview,
    val contacts: List<Contact>,
    val summary: ContactSummary,
) {
    data class Contact(
        val description: String?,
        val documents: List<DocumentReference>,
        val enforcementAction: String?,
        val notes: String?,
        val outcome: String?,
        val sensitive: Boolean?,
        val startDateTime: ZonedDateTime,
        val type: Type,
    ) {
        data class Type(val code: String, val description: String, val systemGenerated: Boolean)

        data class DocumentReference(val id: String, val name: String, val lastUpdated: ZonedDateTime)
    }

    data class ContactSummary(
        val types: List<ContactTypeSummary>,
        val hits: Int,
        val total: Int = types.sumOf { it.total },
    )
}

data class ContactTypeSummary(val code: String, val description: String, val total: Int)

fun Contact.toContact() =
    ContactHistory.Contact(
        description = description,
        documents = documents.map { DocumentReference(it.alfrescoId, it.name, it.lastUpdated) },
        enforcementAction = null,
        notes = notes,
        outcome = outcome?.description,
        sensitive = sensitive,
        startDateTime = date.withTime(startTime),
        type = Type(code = type.code, description = type.description, systemGenerated = type.systemGenerated),
    )

private fun LocalDate.withTime(time: ZonedDateTime?): ZonedDateTime =
    this
        .atTime(time?.withZoneSameInstant(EuropeLondon)?.toLocalTime() ?: LocalTime.MIDNIGHT)
        .atZone(EuropeLondon)
