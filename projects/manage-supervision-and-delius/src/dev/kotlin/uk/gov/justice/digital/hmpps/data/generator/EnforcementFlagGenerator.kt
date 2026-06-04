package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.generateContactTypeOutcome
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EnforcementAction
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

object EnforcementFlagGenerator {

    val PROVIDER = generateProvider("EFG", selectable = true)
    val BOROUGH = generateBorough("EFGB", provider = PROVIDER)
    val DISTRICT = generateDistrict("EFGD", borough = BOROUGH)

    val STAFF = Staff(
        code = "EFGST1",
        forename = "Enforce",
        surname = "Flag",
        provider = PROVIDER,
        caseLoad = emptyList(),
        teams = emptyList(),
        user = null,
        id = IdGenerator.getAndIncrement()
    )

    val TEAM = Team(
        id = IdGenerator.getAndIncrement(),
        code = "EFGTM1",
        description = "Enforcement Flag Test Team",
        staff = listOf(STAFF),
        provider = PROVIDER,
        district = DISTRICT,
        startDate = LocalDate.now()
    )

    val USER = User(
        id = IdGenerator.getAndIncrement(),
        forename = "Enforce",
        surname = "FlagUser",
        staff = STAFF,
        username = "EnforceFlagUser"
    )

    val PERSON = PersonGenerator.generateOverview("EFG0001", forename = "Enforce", surname = "FlagPerson")

    val EVENT = PersonGenerator.generateEvent(
        PERSON,
        eventNumber = "1",
        notes = "Enforcement flag test event",
        additionalOffences = emptyList()
    )

    /**
     * A contact type whose code matches the outcome code below.
     * This is required because setEnforcementFlag looks up a ContactType by the outcome's code.
     * When this type has contactOutcomeFlag=true and the contact has no outcome, enforcementFlag is set to true.
     */
    val OUTCOME_CODE_CONTACT_TYPE = ContactType(
        id = IdGenerator.getAndIncrement(),
        code = "EFGOUT",
        attendanceContact = false,
        description = "EFG Outcome-Code Contact Type",
        contactOutcomeFlag = true,
        locationRequired = "N",
        editable = true,
        offenderContact = true
    )

    /**
     * The actual contact type used to create/update contacts in enforcement flag tests.
     * offenderContact=true so it can be created without an event.
     */
    val CONTACT_TYPE = ContactType(
        id = IdGenerator.getAndIncrement(),
        code = "EFGCT",
        attendanceContact = false,
        description = "EFG Contact Type",
        contactOutcomeFlag = false,
        locationRequired = "N",
        editable = true,
        offenderContact = true
    )

    /**
     * Outcome whose code matches OUTCOME_CODE_CONTACT_TYPE's code ("EFGOUT").
     * This enables setEnforcementFlag to find a ContactType when given this outcome.
     */
    val OUTCOME_MATCHING_CONTACT_TYPE =
        ContactGenerator.generateOutcome("EFGOUT", "EFG Outcome matching CT code", false, false)

    val CONTACT_TYPE_OUTCOME =
        generateContactTypeOutcome(
            CONTACT_TYPE.id,
            OUTCOME_MATCHING_CONTACT_TYPE.id,
            CONTACT_TYPE,
            OUTCOME_MATCHING_CONTACT_TYPE
        )

    /**
     * Contact with no outcome — when setEnforcementFlag is called with OUTCOME_MATCHING_CONTACT_TYPE,
     * it finds OUTCOME_CODE_CONTACT_TYPE (contactOutcomeFlag=true) and contact.outcome==null → enforcementFlag=true.
     */
    val CONTACT_NO_OUTCOME: Contact = ContactGenerator.generateContact(
        person = PERSON,
        contactType = CONTACT_TYPE,
        startDateTime = ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(1), EuropeLondon),
        team = TEAM,
        staff = STAFF,
        event = EVENT
    )

    /**
     * EnforcementAction with outstandingContactAction=true.
     * When a contact has this as its action, setEnforcementFlag will set enforcementFlag=true.
     */
    val OUTSTANDING_ENFORCEMENT_ACTION = EnforcementAction(
        code = "EFGEA1",
        description = "EFG Outstanding Enforcement Action",
        responseByPeriod = 7,
        outstandingContactAction = true,
        selectable = true,
        contactType = CONTACT_TYPE,
        id = IdGenerator.getAndIncrement()
    )

    val OUTSTANDING_EA_OUTCOME_LINK = EnforcementActionContactOutcome(
        EnforcementActionContactOutcomeId(
            enforcementActionId = OUTSTANDING_ENFORCEMENT_ACTION.id,
            contactOutcomeTypeId = OUTCOME_MATCHING_CONTACT_TYPE.id
        )
    )

    /**
     * Contact with an outstanding enforcement action but no outcome, using a contact type valid for
     * updateContact (PATCH). When updateContact is called, setEnforcementFlag should still detect
     * the outstanding action and set enforcementFlag=true, even though contactOutcome is null.
     */
    val CONTACT_WITH_OUTSTANDING_ACTION_NO_OUTCOME: Contact = ContactGenerator.generateContact(
        person = PERSON,
        contactType = ContactGenerator.EMAIL_POP_CT,
        startDateTime = ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(3), EuropeLondon),
        team = TEAM,
        staff = STAFF,
        event = EVENT,
        action = OUTSTANDING_ENFORCEMENT_ACTION
    )

    /**
     * Contact that has an outstanding enforcement action. When updateContactOutcome is called on this
     * contact, setEnforcementFlag detects the outstanding action and sets enforcementFlag=true.
     */
    val CONTACT_WITH_OUTSTANDING_ACTION: Contact = ContactGenerator.generateContact(
        person = PERSON,
        contactType = CONTACT_TYPE,
        startDateTime = ZonedDateTime.of(LocalDateTime.now(EuropeLondon).plusHours(2), EuropeLondon),
        team = TEAM,
        staff = STAFF,
        event = EVENT,
        action = OUTSTANDING_ENFORCEMENT_ACTION
    )

    val OM_STAFF = OffenderManagerStaff(
        id = IdGenerator.getAndIncrement(),
        code = "EFGOMS1",
        forename = "EFG",
        surname = "OffenderManager",
        provider = PROVIDER,
        startDate = LocalDate.now()
    )

    val OM_TEAM = OffenderManagerTeam(
        id = IdGenerator.getAndIncrement(),
        district = DISTRICT,
        provider = PROVIDER,
        code = "EFGOMT",
        description = "EFG OffenderManager Team",
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

