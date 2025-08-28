package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.toCrn
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.entity.staff.Provider
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import java.time.LocalDate
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

    fun Requirement.generateAppointment(
        type: ContactType,
        date: LocalDate,
        startTime: ZonedDateTime? = null,
        endTime: ZonedDateTime? = null,
        staff: Staff,
        team: Team,
        provider: Provider,
    ) = Contact(
        id = 0,
        person = disposal.event.person.toCrn(),
        requirement = this,
        date = date,
        startTime = startTime,
        endTime = endTime,
        type = type,
        staff = staff,
        team = team,
        provider = provider,
        notes = "Some appointment notes",
        externalReference = "urn:uk:gov:hmpps:accredited-programmes:appointment:${UUID.randomUUID()}",
        sensitive = false,
    )

    fun LicenceCondition.generateAppointment(
        type: ContactType,
        date: LocalDate,
        startTime: ZonedDateTime? = null,
        endTime: ZonedDateTime? = null,
        staff: Staff,
        team: Team,
        provider: Provider,
    ) = Contact(
        id = 0,
        person = disposal.event.person.toCrn(),
        licenceCondition = this,
        date = date,
        startTime = startTime,
        endTime = endTime,
        type = type,
        staff = staff,
        team = team,
        provider = provider,
        notes = "Some appointment notes",
        externalReference = "urn:uk:gov:hmpps:accredited-programmes:appointment:${UUID.randomUUID()}",
        sensitive = false,
    )
}
