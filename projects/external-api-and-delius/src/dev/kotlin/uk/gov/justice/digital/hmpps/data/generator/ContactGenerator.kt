package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.JOHN_SMITH
import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import java.time.LocalDate
import java.time.ZonedDateTime

object ContactGenerator {
    val CONTACT_TYPE = ContactType("CNT01", "Contact Type 1", IdGenerator.getAndIncrement())
    val CONTACT_OUTCOME_TYPE = ContactOutcome("CNO01", "Contact Outcome 1", IdGenerator.getAndIncrement())
    val CONTACT = generateContact()
    val MAPPA_CONTACT = generateContact(outcome = null, visorExported = true, description = "Mappa Contact")

    fun generateContact(
        person: Person = PersonGenerator.DEFAULT,
        type: ContactType = CONTACT_TYPE,
        date: LocalDate = LocalDate.now(),
        startTime: ZonedDateTime? = ZonedDateTime.now(),
        team: Team = DEFAULT_TEAM,
        staff: Staff = JOHN_SMITH,
        location: OfficeLocation? = null,
        outcome: ContactOutcome? = CONTACT_OUTCOME_TYPE,
        description: String? = "Description override",
        notes: String? = "Some notes about the contact",
        visorContact: Boolean? = null,
        visorExported: Boolean? = null,
        softDeleted: Boolean = false,
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),
        id: Long = IdGenerator.getAndIncrement(),
    ) = Contact(
        person,
        type,
        date,
        startTime,
        team,
        staff,
        location,
        outcome,
        description,
        notes,
        visorContact,
        visorExported,
        softDeleted,
        createdDateTime,
        lastUpdatedDateTime,
        id
    )
}