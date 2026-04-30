package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.generateContactTypeOutcome
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.entity.Team
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

object UpdateContactOutcomeGenerator {

    val PROVIDER = generateProvider("UCO", selectable = true)
    val BOROUGH = generateBorough("UCOB", provider = PROVIDER)
    val DISTRICT = generateDistrict("UCOD", borough = BOROUGH)

    val STAFF = Staff(
        code = "UCOST1",
        forename = "Outcome",
        surname = "Staff",
        provider = PROVIDER,
        caseLoad = emptyList(),
        teams = emptyList(),
        user = null,
        id = IdGenerator.getAndIncrement()
    )

    val TEAM = Team(
        id = IdGenerator.getAndIncrement(),
        code = "UCOTM1",
        description = "Update Contact Outcome Team",
        staff = listOf(STAFF),
        provider = PROVIDER,
        district = DISTRICT,
        startDate = LocalDate.now()
    )

    val USER = User(
        id = IdGenerator.getAndIncrement(),
        forename = "Outcome",
        surname = "Updater",
        staff = STAFF,
        username = "OutcomeUpdater"
    )

    val personId = IdGenerator.getAndIncrement()
    val PERSON = PersonGenerator.generateOverview("UCO0001", forename = "Outcome", surname = "Person", id = personId)

    val EVENT = PersonGenerator.generateEvent(
        PERSON,
        eventNumber = "1",
        notes = "Update contact outcome event",
        additionalOffences = emptyList()
    )

    val CONTACT_TYPE = ContactType(
        id = IdGenerator.getAndIncrement(),
        code = "UCOCTC1",
        attendanceContact = true,
        description = "Update Contact Outcome Type",
        contactOutcomeFlag = true,
        locationRequired = "N",
        editable = true
    )

    val OUTCOME = ContactGenerator.generateOutcome("UCOOUT", "UCO Acceptable Absence", false, true)

    val CONTACT_TYPE_OUTCOME = generateContactTypeOutcome(CONTACT_TYPE.id, OUTCOME.id, CONTACT_TYPE, OUTCOME)

    val CONTACT_1 = ContactGenerator.generateContact(
        PERSON,
        CONTACT_TYPE,
        ZonedDateTime.of(LocalDateTime.now(EuropeLondon).minusHours(1), EuropeLondon),
        team = TEAM,
        staff = STAFF,
        event = EVENT
    )

    val CONTACT_2 = ContactGenerator.generateContact(
        PERSON,
        CONTACT_TYPE,
        ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(3), EuropeLondon),
        team = TEAM,
        staff = STAFF,
        event = EVENT
    )

    val OFFENDER_MANAGER = OffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PERSON,
        provider = ContactGenerator.DEFAULT_PROVIDER,
        team = OffenderManagerGenerator.TEAM,
        staff = OffenderManagerGenerator.STAFF_1,
        allocationDate = LocalDate.now(),
        lastUpdated = ZonedDateTime.now()
    )

    val RESPONSIBLE_OFFICER = ResponsibleOfficer(
        id = IdGenerator.getAndIncrement(),
        personId = PERSON.id,
        startDate = ZonedDateTime.now(),
        endDate = ZonedDateTime.now(),
        offenderManagerId = OFFENDER_MANAGER.id
    )
}
