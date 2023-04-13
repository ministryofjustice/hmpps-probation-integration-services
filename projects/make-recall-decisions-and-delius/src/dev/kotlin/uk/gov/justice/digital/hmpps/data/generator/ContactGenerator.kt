package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.ContactDocument
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.ContactType
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate.EPOCH
import java.time.ZonedDateTime

object ContactGenerator {
    val DEFAULT_OUTCOME = ContactOutcome(IdGenerator.getAndIncrement(), "Contact outcome")
    val DEFAULT_TYPE = ContactType(IdGenerator.getAndIncrement(), "TYPE", "Contact type", false)
    val SYSTEM_GENERATED_TYPE = ContactType(IdGenerator.getAndIncrement(), "SG", "System-generated contact type", true)

    val DEFAULT = generate(notes = "default")
    val SYSTEM_GENERATED = generate(notes = "system-generated", ZonedDateTime.now().minusDays(1), SYSTEM_GENERATED_TYPE)
    val WITH_DOCUMENTS = generate(notes = "documents", ZonedDateTime.now().minusDays(2)).set(Contact::documents) {
        listOf(
            ContactDocument(IdGenerator.getAndIncrement(), it, "uuid1", "doc1", ZonedDateTime.now()),
            ContactDocument(IdGenerator.getAndIncrement(), it, "uuid2", "doc2", ZonedDateTime.now())
        )
    }
    val PAST = generate(notes = "past", ZonedDateTime.of(2022, 1, 1, 12, 0, 0, 0, EuropeLondon))
    val FUTURE = generate(notes = "future", ZonedDateTime.now().plusDays(1))

    fun generate(
        notes: String = "Contact notes",
        dateTime: ZonedDateTime = ZonedDateTime.now(),
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
        date = dateTime.toLocalDate(),
        startTime = dateTime.toLocalTime().atDate(EPOCH).atZone(dateTime.zone),
        sensitive = false
    )
}
