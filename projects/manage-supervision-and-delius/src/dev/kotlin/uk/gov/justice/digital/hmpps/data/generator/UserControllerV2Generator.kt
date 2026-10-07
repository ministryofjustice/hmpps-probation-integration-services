package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.generateContactTypeOutcome
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.entity.Team
import java.time.LocalDate
import java.time.ZonedDateTime

object UserControllerV2Generator {
    val PROVIDER = generateProvider("V20", description = "V2 Controller Provider", selectable = true)
    val BOROUGH = generateBorough("V2B", description = "V2 Controller Borough", provider = PROVIDER)
    val DISTRICT = generateDistrict("V2D", description = "V2 Controller District", borough = BOROUGH)
    val OFFICE_LOCATION = ContactGenerator.generateOfficeLocation(
        code = "V2_OFF",
        description = "V2 Controller Office",
        buildingNumber = "20",
        streetName = "Version Street",
        town = "London",
        postcode = "V20 1AA",
        ldu = DISTRICT,
        provider = PROVIDER,
        startDate = LocalDate.of(2030, 1, 1)
    )

    val STAFF = Staff(
        code = "V2USR01",
        forename = "Veronica",
        surname = "Tester",
        provider = PROVIDER,
        caseLoad = emptyList(),
        teams = emptyList(),
        id = IdGenerator.getAndIncrement()
    )

    val TEAM = Team(
        id = IdGenerator.getAndIncrement(),
        code = "V2T001",
        description = "V2 Controller Team",
        staff = listOf(STAFF),
        provider = PROVIDER,
        district = DISTRICT,
        startDate = LocalDate.of(2030, 1, 1)
    )

    val USER = User(
        id = IdGenerator.getAndIncrement(),
        staff = STAFF,
        username = "v2-controller-user",
        forename = "Veronica",
        surname = "Tester"
    )

    val PERSON = PersonGenerator.generateOverview(
        crn = "V200001",
        forename = "Vera",
        secondName = "Integration",
        thirdName = null,
        surname = "Future"
    )

    val EVENT = PersonGenerator.generateEvent(
        person = PERSON,
        eventNumber = "2001",
        notes = "v2 controller test event",
        additionalOffences = emptyList(),
        dateCreated = ZonedDateTime.of(2034, 12, 1, 9, 0, 0, 0, EuropeLondon)
    )

    val DISPOSAL_TYPE = PersonGenerator.generateDisposalType("V2S", "V2 Independent Sentence")
    val DISPOSAL = PersonGenerator.generateDisposal(
        event = EVENT,
        type = DISPOSAL_TYPE,
        date = LocalDate.of(2034, 12, 2)
    )

    val CONTACT_TYPE = ContactType(
        id = IdGenerator.getAndIncrement(),
        code = "V2AP1",
        attendanceContact = true,
        description = "V2 Independent Appointment",
        systemGenerated = false,
        nationalStandardsContact = false,
        contactOutcomeFlag = true,
        offenderContact = false,
        eventContact = false,
        locationRequired = "Y",
        editable = true
    )

    val SELECTABLE_OUTCOME: ContactOutcome = ContactGenerator.generateOutcome(
        code = "V2O",
        description = "V2 Selectable Outcome",
        attendance = true,
        acceptable = true
    )

    val CONTACT_TYPE_OUTCOME = generateContactTypeOutcome(
        CONTACT_TYPE.id,
        SELECTABLE_OUTCOME.id,
        CONTACT_TYPE,
        SELECTABLE_OUTCOME
    )

    val UPCOMING_FILTER_DATE_TIME: ZonedDateTime = ZonedDateTime.of(2035, 1, 15, 10, 30, 0, 0, EuropeLondon)

    val BEFORE_REFERENCE_CONTACT = generateContact(
        description = "before reference",
        startDateTime = ZonedDateTime.of(2035, 1, 15, 9, 0, 0, 0, EuropeLondon)
    )
    val EXACT_REFERENCE_CONTACT = generateContact(
        description = "at reference",
        startDateTime = ZonedDateTime.of(2035, 1, 15, 10, 30, 0, 0, EuropeLondon)
    )
    val AFTER_REFERENCE_CONTACT = generateContact(
        description = "after reference",
        startDateTime = ZonedDateTime.of(2035, 1, 15, 11, 0, 0, 0, EuropeLondon)
    )
    val NEXT_DAY_CONTACT = generateContact(
        description = "next day",
        startDateTime = ZonedDateTime.of(2035, 1, 16, 8, 15, 0, 0, EuropeLondon)
    )
    val FUTURE_CONTACT_WITH_OUTCOME = generateContact(
        description = "future with outcome",
        startDateTime = ZonedDateTime.of(2035, 1, 17, 12, 0, 0, 0, EuropeLondon),
        outcome = SELECTABLE_OUTCOME
    )
    val HISTORIC_CONTACT = generateContact(
        description = "historic",
        startDateTime = ZonedDateTime.of(2025, 1, 10, 14, 0, 0, 0, EuropeLondon)
    )

    private fun generateContact(
        description: String,
        startDateTime: ZonedDateTime,
        outcome: ContactOutcome? = null
    ) = Contact(
        id = IdGenerator.getAndIncrement(),
        person = PERSON,
        event = EVENT,
        type = CONTACT_TYPE,
        date = startDateTime.toLocalDate(),
        startTime = ZonedDateTime.of(LocalDate.EPOCH, startDateTime.toLocalTime(), startDateTime.zone),
        attended = true,
        sensitive = false,
        complied = true,
        outcome = outcome,
        notes = null,
        description = description,
        team = TEAM,
        staff = STAFF,
        provider = PROVIDER,
        location = OFFICE_LOCATION,
        createdDateTime = ZonedDateTime.of(2034, 12, 1, 9, 0, 0, 0, EuropeLondon),
        lastUpdated = ZonedDateTime.of(2034, 12, 1, 9, 0, 0, 0, EuropeLondon),
        lastUpdatedUser = USER
    )
}


