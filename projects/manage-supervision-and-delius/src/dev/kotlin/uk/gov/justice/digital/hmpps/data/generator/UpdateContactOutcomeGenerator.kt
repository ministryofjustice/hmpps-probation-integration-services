package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.generateContactTypeOutcome
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Staff as OffenderManagerStaff
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Team as OffenderManagerTeam
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

    val ENFORCEMENT_ACTION =
        ContactGenerator.generateEnforcementAction("UCOENF1", "UCO Enforcement Action", CONTACT_TYPE)

    val CONTACT_3 = ContactGenerator.generateContact(
        PERSON,
        CONTACT_TYPE,
        ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(5), EuropeLondon),
        team = TEAM,
        staff = STAFF,
        event = EVENT
    )

    val CONTACT_4 = ContactGenerator.generateContact(
        PERSON,
        CONTACT_TYPE,
        ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(6), EuropeLondon),
        team = TEAM,
        staff = STAFF,
        event = EVENT
    )

    val PERSON_NO_MANAGER = PersonGenerator.generateOverview("UCO0002", forename = "NoManager", surname = "Person")

    val CONTACT_NO_MANAGER = ContactGenerator.generateContact(
        PERSON_NO_MANAGER,
        CONTACT_TYPE,
        ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(7), EuropeLondon),
        team = TEAM,
        staff = STAFF,
        event = EVENT
    )

    val OM_STAFF = OffenderManagerStaff(
        id = IdGenerator.getAndIncrement(),
        code = "UCOM001",
        forename = "UCO",
        surname = "Manager",
        provider = PROVIDER,
        startDate = LocalDate.now()
    )

    val OM_TEAM = OffenderManagerTeam(
        id = IdGenerator.getAndIncrement(),
        district = DISTRICT,
        provider = PROVIDER,
        code = "UCOOMT",
        description = "UCO OffenderManager Team",
        startDate = LocalDate.now()
    )

    val OFFENDER_MANAGER = OffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = PERSON,
        provider = PROVIDER,
        team = OM_TEAM,
        staff = OM_STAFF,
        allocationDate = LocalDate.now(),
        lastUpdated = ZonedDateTime.now()
    )

    val RESPONSIBLE_OFFICER = ResponsibleOfficer(
        id = IdGenerator.getAndIncrement(),
        personId = PERSON.id,
        startDate = ZonedDateTime.now(),
        endDate = null,
        offenderManagerId = OFFENDER_MANAGER.id
    )
}
