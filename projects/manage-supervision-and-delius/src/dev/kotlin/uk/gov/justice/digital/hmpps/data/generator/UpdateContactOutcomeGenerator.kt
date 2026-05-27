package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.generateContactTypeOutcome
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EnforcementActionContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EnforcementActionContactOutcomeId
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.entity.Team
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Staff as OffenderManagerStaff
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Team as OffenderManagerTeam

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

    val ARWS_CONTACT_TYPE = ContactType(
        id = IdGenerator.getAndIncrement(),
        code = "ARWS",
        attendanceContact = false,
        description = "Review Enforcement Status",
        contactOutcomeFlag = false,
        locationRequired = "N",
        editable = false
    )

    val CONTACT_TYPE = ContactType(
        id = IdGenerator.getAndIncrement(),
        code = "CCON",
        attendanceContact = true,
        description = "Update Contact Outcome Type",
        contactOutcomeFlag = true,
        locationRequired = "N",
        editable = true,
        nationalStandardsContact = true
    )

    val OUTCOME = ContactGenerator.generateOutcome("UCOOUT", "UCO Non-Compliant", false, false)

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

    val ENFORCEMENT_ACTION_OUTCOME_TYPE = EnforcementActionContactOutcome(
        EnforcementActionContactOutcomeId(
            enforcementActionId = ENFORCEMENT_ACTION.id,
            contactOutcomeTypeId = OUTCOME.id
        )
    )

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

    // A person-level contact type (offenderContact = true) — not linked to an event
    val PERSON_LEVEL_CONTACT_TYPE = ContactType(
        id = IdGenerator.getAndIncrement(),
        code = "UPCT",
        attendanceContact = false,
        description = "UCO Person Level Contact Type",
        contactOutcomeFlag = true,
        locationRequired = "N",
        editable = true,
        offenderContact = true
    )

    val PERSON_LEVEL_OUTCOME = ContactGenerator.generateOutcome("UPLOUT", "UCO Person Level Outcome", false, true)

    val PERSON_LEVEL_CONTACT_TYPE_OUTCOME =
        generateContactTypeOutcome(
            PERSON_LEVEL_CONTACT_TYPE.id,
            PERSON_LEVEL_OUTCOME.id,
            PERSON_LEVEL_CONTACT_TYPE,
            PERSON_LEVEL_OUTCOME
        )

    val PERSON_LEVEL_ENFORCEMENT_ACTION =
        ContactGenerator.generateEnforcementAction("UCOPENF", "UCO Person Level Enforcement", CONTACT_TYPE)

    val PERSON_LEVEL_ENFORCEMENT_ACTION_OUTCOME_TYPE = EnforcementActionContactOutcome(
        EnforcementActionContactOutcomeId(
            enforcementActionId = PERSON_LEVEL_ENFORCEMENT_ACTION.id,
            contactOutcomeTypeId = PERSON_LEVEL_OUTCOME.id
        )
    )

    val CONTACT_5 = Contact(
        id = IdGenerator.getAndIncrement(),
        person = PERSON,
        type = PERSON_LEVEL_CONTACT_TYPE,
        date = ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(8), EuropeLondon).toLocalDate(),
        startTime = ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(8), EuropeLondon),
        team = TEAM,
        staff = STAFF,
        event = null,
        outcome = PERSON_LEVEL_OUTCOME,
        notes = null,
    )

    val CONTACT_6 = ContactGenerator.generateContact(
        PERSON,
        CONTACT_TYPE,
        ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(9), EuropeLondon),
        team = TEAM,
        staff = STAFF,
        event = EVENT,
        outcome = OUTCOME
    )

    // Event with a disposal whose ftcLimit is 1 and ftcCount already at 1 — next enforcement triggers ARWS review
    val FTC_DISPOSAL_TYPE = PersonGenerator.generateDisposalType("UCOFTC", "UCO FTC Disposal Type", ftcLimit = 1)
    val FTC_EVENT = PersonGenerator.generateEvent(
        PERSON,
        eventNumber = "2",
        notes = "FTC limit event",
        additionalOffences = emptyList(),
    ).also { it.ftcCount = 1 }
    val FTC_DISPOSAL = PersonGenerator.generateDisposal(FTC_EVENT, type = FTC_DISPOSAL_TYPE)

    // A pre-existing non-compliant contact on FTC_EVENT so countFailureToComply returns 1 before the test call
    val FTC_PRIOR_CONTACT = ContactGenerator.generateContact(
        PERSON,
        CONTACT_TYPE,
        ZonedDateTime.of(LocalDateTime.now(EuropeLondon).minusDays(1), EuropeLondon),
        complied = false,
        team = TEAM,
        staff = STAFF,
        event = FTC_EVENT,
    )

    val CONTACT_7 = ContactGenerator.generateContact(
        PERSON,
        CONTACT_TYPE,
        ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(10), EuropeLondon),
        team = TEAM,
        staff = STAFF,
        event = FTC_EVENT,
        outcome = OUTCOME
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

    // --- Completely fresh data for enforcement-deletion-on-compliant-outcome test ---

    val CMP_PROVIDER = generateProvider("UCC", selectable = true)
    val CMP_BOROUGH = generateBorough("UCCB", provider = CMP_PROVIDER)
    val CMP_DISTRICT = generateDistrict("UCCD", borough = CMP_BOROUGH)

    val CMP_STAFF = Staff(
        code = "UCCST1",
        forename = "Compliant",
        surname = "Staff",
        provider = CMP_PROVIDER,
        caseLoad = emptyList(),
        teams = emptyList(),
        user = null,
        id = IdGenerator.getAndIncrement()
    )

    val CMP_TEAM = Team(
        id = IdGenerator.getAndIncrement(),
        code = "UCCTM1",
        description = "UCO Compliant Test Team",
        staff = listOf(CMP_STAFF),
        provider = CMP_PROVIDER,
        district = CMP_DISTRICT,
        startDate = LocalDate.now()
    )

    val CMP_PERSON = PersonGenerator.generateOverview("UCO0003", forename = "Compliant", surname = "TestPerson")

    val CMP_EVENT = PersonGenerator.generateEvent(
        CMP_PERSON,
        eventNumber = "1",
        notes = "Compliant outcome test event",
        additionalOffences = emptyList()
    )

    val CMP_CONTACT_TYPE = ContactType(
        id = IdGenerator.getAndIncrement(),
        code = "UCCC",
        attendanceContact = true,
        description = "UCO Compliant Contact Type",
        contactOutcomeFlag = true,
        locationRequired = "N",
        editable = true,
        nationalStandardsContact = true
    )

    val CMP_NON_COMPLIANT_OUTCOME = ContactGenerator.generateOutcome("UCCNCO", "UCC Non-Compliant Outcome", false, false)
    val CMP_COMPLIANT_OUTCOME = ContactGenerator.generateOutcome("UCCCMP", "UCC Compliant Outcome", true, true)

    val CMP_CONTACT_TYPE_NON_COMPLIANT_OUTCOME =
        generateContactTypeOutcome(CMP_CONTACT_TYPE.id, CMP_NON_COMPLIANT_OUTCOME.id, CMP_CONTACT_TYPE, CMP_NON_COMPLIANT_OUTCOME)

    val CMP_CONTACT_TYPE_COMPLIANT_OUTCOME =
        generateContactTypeOutcome(CMP_CONTACT_TYPE.id, CMP_COMPLIANT_OUTCOME.id, CMP_CONTACT_TYPE, CMP_COMPLIANT_OUTCOME)

    val CMP_ENFORCEMENT_ACTION =
        ContactGenerator.generateEnforcementAction("UCCENF1", "UCC Enforcement Action", CMP_CONTACT_TYPE)

    val CMP_ENFORCEMENT_ACTION_OUTCOME_TYPE = EnforcementActionContactOutcome(
        EnforcementActionContactOutcomeId(
            enforcementActionId = CMP_ENFORCEMENT_ACTION.id,
            contactOutcomeTypeId = CMP_NON_COMPLIANT_OUTCOME.id
        )
    )

    val CMP_OM_STAFF = OffenderManagerStaff(
        id = IdGenerator.getAndIncrement(),
        code = "UCCOMS1",
        forename = "UCC",
        surname = "OffenderManager",
        provider = CMP_PROVIDER,
        startDate = LocalDate.now()
    )

    val CMP_OM_TEAM = OffenderManagerTeam(
        id = IdGenerator.getAndIncrement(),
        district = CMP_DISTRICT,
        provider = CMP_PROVIDER,
        code = "UCCOMT",
        description = "UCC OffenderManager Team",
        startDate = LocalDate.now()
    )

    val CMP_OFFENDER_MANAGER = OffenderManager(
        id = IdGenerator.getAndIncrement(),
        person = CMP_PERSON,
        provider = CMP_PROVIDER,
        team = CMP_OM_TEAM,
        staff = CMP_OM_STAFF,
        allocationDate = LocalDate.now(),
        lastUpdated = ZonedDateTime.now()
    )

    val CMP_RESPONSIBLE_OFFICER = ResponsibleOfficer(
        id = IdGenerator.getAndIncrement(),
        personId = CMP_PERSON.id,
        startDate = ZonedDateTime.now(),
        endDate = null,
        offenderManagerId = CMP_OFFENDER_MANAGER.id
    )

    // Contact with complied=false and a pre-existing enforcement — updating to compliant outcome should delete the enforcement
    val CONTACT_8 = ContactGenerator.generateContact(
        CMP_PERSON,
        CMP_CONTACT_TYPE,
        ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(1), EuropeLondon),
        complied = false,
        team = CMP_TEAM,
        staff = CMP_STAFF,
        event = CMP_EVENT,
        outcome = CMP_NON_COMPLIANT_OUTCOME
    )

    val ENFORCEMENT_FOR_CONTACT_8 = ContactGenerator.generateEnforcement(
        contact = CONTACT_8,
        action = CMP_ENFORCEMENT_ACTION
    )
}
