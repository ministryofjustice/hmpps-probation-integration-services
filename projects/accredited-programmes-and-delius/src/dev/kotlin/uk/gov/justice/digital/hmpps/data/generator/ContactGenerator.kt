package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.TestData.ATTENDED_COMPLIED
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.toCrn
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.entity.staff.Provider
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

object ContactGenerator {
    fun Event.contact(
        type: ContactType,
        date: LocalDate,
        staff: Staff,
        team: Team,
        provider: Provider,
        startTime: ZonedDateTime? = null,
        endTime: ZonedDateTime? = null,
    ) = Contact(
        id = 0,
        person = person.toCrn(),
        event = this,
        date = date,
        startTime = startTime,
        endTime = endTime,
        type = type,
        staff = staff,
        team = team,
        provider = provider,
        notes = null,
        externalReference = null,
        sensitive = false,
    )

    fun Requirement.contact(
        type: ContactType,
        staff: Staff,
        team: Team,
        provider: Provider,
        date: LocalDate,
        startTime: LocalTime? = LocalTime.now(),
        endTime: LocalTime? = startTime?.plusHours(1),
    ) = Contact(
        id = 0,
        person = disposal.event.person.toCrn(),
        event = disposal.event,
        requirement = this,
        date = date,
        startTime = startTime?.let { date.atTime(it).atZone(EuropeLondon) },
        endTime = endTime?.let { date.atTime(it).atZone(EuropeLondon) },
        type = type,
        staff = staff,
        team = team,
        provider = provider,
        notes = "Some appointment notes",
        externalReference = "${Contact.REFERENCE_PREFIX}${UUID.randomUUID()}",
        sensitive = false,
    )

    fun Contact.withOutcome() = apply {
        outcome = ATTENDED_COMPLIED
        attended = true
        complied = true
    }

    fun LicenceCondition.contact(
        type: ContactType,
        staff: Staff,
        team: Team,
        provider: Provider,
        date: LocalDate,
        startTime: LocalTime? = LocalTime.now(),
        endTime: LocalTime? = startTime?.plusHours(1),
    ) = Contact(
        id = 0,
        person = disposal.event.person.toCrn(),
        event = disposal.event,
        licenceCondition = this,
        date = date,
        startTime = startTime?.let { date.atTime(it).atZone(EuropeLondon) },
        endTime = endTime?.let { date.atTime(it).atZone(EuropeLondon) },
        type = type,
        staff = staff,
        team = team,
        provider = provider,
        notes = "Some appointment notes",
        externalReference = "${Contact.REFERENCE_PREFIX}${UUID.randomUUID()}",
        sensitive = false,
    )
}
