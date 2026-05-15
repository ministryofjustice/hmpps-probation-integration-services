package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

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
        ftcCount = null,
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
        ftcCount = null,
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
}

