package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.compliance.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Completely self-contained test data used only by the months filter integration test.
 * Nothing in here references data from any other generator.
 */
object MonthsFilterGenerator {

    val GENDER = ReferenceData(IdGenerator.getAndIncrement(), "M", "Male (months filter)")
    val DISPOSAL_TYPE = DisposalType(
        code = "MFT",
        description = "Months Filter Sentence Type",
        sentenceType = "NP",
        ftcLimit = 0,
        id = IdGenerator.getAndIncrement()
    )

    val PERSON = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "X000099",
        forename = "Months",
        secondName = null,
        thirdName = null,
        surname = "Filter",
        dateOfBirth = LocalDate.now().minusYears(30),
        dateOfDeath = null,
        gender = GENDER,
        emailAddress = null,
        mobileNumber = null,
        telephoneNumber = null,
        preferredName = null,
        pnc = null,
        noms = null,
        religion = null,
        sexualOrientation = null,
        genderIdentity = null,
        genderIdentityDescription = null,
        exclusionMessage = null,
        restrictionMessage = null
    )

    val RECENT_OFFENCE = Offence(
        id = IdGenerator.getAndIncrement(),
        code = "MF001",
        description = "Months Filter Recent Offence",
        category = "Months Filter"
    )

    val OLD_OFFENCE = Offence(
        id = IdGenerator.getAndIncrement(),
        code = "MF002",
        description = "Months Filter Old Offence",
        category = "Months Filter"
    )

    val RECENT_EVENT = Event(
        id = IdGenerator.getAndIncrement(),
        personId = PERSON.id,
        court = null,
        convictionDate = LocalDate.now(),
        eventNumber = "9900001",
        ftcCount = 0,
        inBreach = false,
        active = true,
        notes = "months filter recent event",
        additionalOffences = emptyList(),
        dateCreated = ZonedDateTime.now(),
        lastUpdatedDateTime = ZonedDateTime.now()
    )

    val OLD_EVENT = Event(
        id = IdGenerator.getAndIncrement(),
        personId = PERSON.id,
        court = null,
        convictionDate = LocalDate.now().minusMonths(14),
        eventNumber = "9900002",
        ftcCount = 0,
        inBreach = false,
        active = true,
        notes = "months filter old event",
        additionalOffences = emptyList(),
        dateCreated = ZonedDateTime.now().minusMonths(13),
        lastUpdatedDateTime = ZonedDateTime.now().minusMonths(13)
    )

    val RECENT_MAIN_OFFENCE = MainOffence(
        id = IdGenerator.getAndIncrement(),
        offenceCount = 1,
        event = RECENT_EVENT,
        date = LocalDate.now(),
        offence = RECENT_OFFENCE
    )

    val OLD_MAIN_OFFENCE = MainOffence(
        id = IdGenerator.getAndIncrement(),
        offenceCount = 1,
        event = OLD_EVENT,
        date = LocalDate.now().minusMonths(14),
        offence = OLD_OFFENCE
    )

    val RECENT_DISPOSAL = Disposal(
        event = RECENT_EVENT,
        personId = PERSON.id,
        date = LocalDate.now().minusDays(7),
        length = 12,
        type = DISPOSAL_TYPE,
        terminationReason = null,
        lengthUnit = null,
        enteredEndDate = null,
        notionalEndDate = null,
        terminationDate = null,
        active = true,
        softDeleted = false,
        id = IdGenerator.getAndIncrement()
    )

    val OLD_DISPOSAL = Disposal(
        event = OLD_EVENT,
        personId = PERSON.id,
        date = LocalDate.now().minusMonths(14),
        length = 12,
        type = DISPOSAL_TYPE,
        terminationReason = null,
        lengthUnit = null,
        enteredEndDate = null,
        notionalEndDate = null,
        terminationDate = null,
        active = true,
        softDeleted = false,
        id = IdGenerator.getAndIncrement()
    )

    // NSI (breach) data — used to verify the months filter applies to breach counts
    val NSI_BREACH_TYPE = NsiType(
        code = "BRE",
        description = "Breach (months filter)",
        id = IdGenerator.getAndIncrement()
    )
    val NSI_STATUS = NsiStatus(
        id = IdGenerator.getAndIncrement(),
        code = "MF_STATUS",
        description = "Months Filter NSI Status"
    )

    /** Breach that started within the 6-month window */
    val RECENT_BREACH = Nsi(
        id = IdGenerator.getAndIncrement(),
        personId = PERSON.id,
        eventId = RECENT_EVENT.id,
        type = NSI_BREACH_TYPE,
        nsiStatus = NSI_STATUS,
        actualStartDate = LocalDate.now().minusMonths(2),
        expectedStartDate = LocalDate.now().minusMonths(2),
        active = false,
        lastUpdated = ZonedDateTime.now().minusMonths(2).truncatedTo(ChronoUnit.SECONDS)
    )

    /** Breach that started outside the 6-month window */
    val OLD_BREACH = Nsi(
        id = IdGenerator.getAndIncrement(),
        personId = PERSON.id,
        eventId = OLD_EVENT.id,
        type = NSI_BREACH_TYPE,
        nsiStatus = NSI_STATUS,
        actualStartDate = LocalDate.now().minusMonths(10),
        expectedStartDate = LocalDate.now().minusMonths(10),
        active = false,
        lastUpdated = ZonedDateTime.now().minusMonths(10).truncatedTo(ChronoUnit.SECONDS)
    )
}

