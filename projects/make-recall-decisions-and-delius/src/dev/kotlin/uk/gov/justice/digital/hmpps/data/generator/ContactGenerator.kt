package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.ContactDocument
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.ContactType
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate
import java.time.LocalDate.EPOCH
import java.time.LocalTime
import java.time.ZonedDateTime

object ContactGenerator {
    val DEFAULT_OUTCOME = ContactOutcome(IdGenerator.getAndIncrement(), "Contact outcome")
    val DEFAULT_TYPE = ContactType(IdGenerator.getAndIncrement(), "TYPE", "Contact type", false)
    val SYSTEM_GENERATED_TYPE = ContactType(IdGenerator.getAndIncrement(), "SG", "System-generated contact type", true)
    val AP_RESIDENCE_PLAN_PREPARED =
        ContactType(IdGenerator.getAndIncrement(), "APRAP5", "AP Residence Plan Prepared", false)
    val CONSIDERATION =
        ContactType(IdGenerator.getAndIncrement(), "C519", "Recall being considered - CaR", false)

    val DEFAULT = generate(notes = "default")
    val SYSTEM_GENERATED =
        generate(notes = "system-generated", LocalDate.now().minusDays(1), type = SYSTEM_GENERATED_TYPE)
    val WITH_DOCUMENTS = generate(notes = "documents", LocalDate.now().minusDays(2)).set(Contact::documents) {
        listOf(
            generateContactDocument(it, "00000000-0000-0000-0000-000000000001", "doc1"),
            generateContactDocument(it, "00000000-0000-0000-0000-000000000002", "doc2")
        )
    }
    val AP_RESIDENCE_PLAN_PREPARED_CONTACT =
        generate(notes = "AP Residence Plan Prepared", LocalDate.now().minusDays(3), type = AP_RESIDENCE_PLAN_PREPARED)

    val PAST = generate(notes = "past", LocalDate.of(2022, 1, 1), LocalTime.NOON)
    val FUTURE = generate(notes = "future", LocalDate.now().plusDays(1))

    fun generate(
        notes: String = "Contact notes",
        date: LocalDate = LocalDate.now(),
        time: LocalTime? = LocalTime.now(),
        type: ContactType = DEFAULT_TYPE,
        personId: Long = PersonGenerator.CASE_SUMMARY.id,
        id: Long = IdGenerator.getAndIncrement()
    ) = Contact(
        id = id,
        personId = personId,
        description = "Contact description",
        type = type,
        outcome = DEFAULT_OUTCOME,
        documents = listOf(),
        notes = notes,
        date = date,
        startTime = time?.atDate(EPOCH)?.atZone(EuropeLondon),
        sensitive = false
    )

    fun generateContactDocument(
        contact: Contact,
        alfrescoId: String,
        filename: String,
        personId: Long = PersonGenerator.CASE_SUMMARY.id,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactDocument(id, personId, contact, alfrescoId, filename, ZonedDateTime.now())
}
