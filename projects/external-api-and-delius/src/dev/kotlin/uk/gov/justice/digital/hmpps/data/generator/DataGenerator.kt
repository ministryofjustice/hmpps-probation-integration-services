package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DATASET_TYPE_OTHER
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

object DataGenerator {
    val DEFAULT_PROVIDER = Provider("DEF", "Default Provider", id())
    val DEFAULT_PDU = Pdu(DEFAULT_PROVIDER, "DEFPDU", "Default PDU", id())
    val DEFAULT_LAU = Lau(DEFAULT_PDU, "DEFLAU", "Default Lau", id())
    val DEFAULT_TEAM =
        Team("DEFUAT", "Default Team", "020 334 1257", "team@justice.co.uk", DEFAULT_LAU, id())
    val JOHN_SMITH = Staff("DEFJOSM", "John", "Smith", null, id())
    val JS_USER = StaffUser(JOHN_SMITH, "john-smith", id())

    val PERSON = PersonGenerator.DEFAULT
    val PERSON_2 = PersonGenerator.DEFAULT_2
    val PERSON_MANAGER = PersonManager(
        PERSON,
        DEFAULT_PROVIDER,
        DEFAULT_TEAM,
        JOHN_SMITH,
        true,
        id()
    )

    val PERSON_MANAGER_2 = PersonManager(
        PERSON_2,
        DEFAULT_PROVIDER,
        DEFAULT_TEAM,
        JOHN_SMITH,
        true,
        id()
    )

    val EXCLUSION_PERSON_MANAGER = PersonGenerator.generateManager(PersonGenerator.EXCLUSION)
    val RESTRICTION_PERSON_MANAGER = PersonGenerator.generateManager(PersonGenerator.RESTRICTION)

    val OFFENCE = Offence(
        id = id(),
        code = "12345",
        description = "Test offence",
        mainCategoryCode = "123",
        mainCategoryDescription = "Test",
        subCategoryCode = "45",
        subCategoryDescription = "offence",
        schedule15SexualOffence = true,
        schedule15ViolentOffence = null
    )

    val COURT = Court(id = id(), name = "Manchester Crown Court")
    val COURT_APPEARANCE_TYPE =
        ReferenceData(
            id = id(),
            code = "SEN",
            description = "Sentence",
            dataset = DATASET_TYPE_OTHER
        )
    val COURT_APPEARANCE_PLEA =
        ReferenceData(
            id = id(),
            code = "GLT",
            description = "Not guilty",
            dataset = DATASET_TYPE_OTHER
        )
    val DISPOSAL_TYPE =
        DisposalType(id = id(), description = "ORA Suspended Sentence Order", "SC")
    val MONTHS = ReferenceData(
        id = id(),
        code = "MNTH",
        description = "Months",
        dataset = DATASET_TYPE_OTHER
    )
    val LENGTH_UNIT_NA = ReferenceData(
        id = id(),
        code = "NA",
        description = "Non applicable",
        dataset = DATASET_TYPE_OTHER
    )

    val EVENT = Event(
        id = id(),
        person = PERSON,
        number = "1",
        convictionDate = LocalDate.of(2023, 1, 2),
        mainOffence = MainOffence(
            id = id(),
            date = LocalDate.of(2023, 1, 1),
            count = 1,
            offence = OFFENCE
        ),
        additionalOffences = listOf(
            AdditionalOffence(
                id = id(),
                date = null,
                count = 3,
                offence = OFFENCE
            )
        ),
        courtAppearances = listOf(
            CourtAppearance(
                id = id(),
                date = ZonedDateTime.of(LocalDate.of(2023, 2, 3), LocalTime.of(10, 0, 0), EuropeLondon),
                court = COURT,
                type = COURT_APPEARANCE_TYPE,
                plea = COURT_APPEARANCE_PLEA
            )
        ),
        disposal = Disposal(
            id = id(),
            type = DISPOSAL_TYPE,
            date = LocalDate.of(2023, 3, 4),
            length = 6,
            lengthUnits = MONTHS
        )
    ).also { event ->
        event.mainOffence.set(MainOffence::event, event)
        event.additionalOffences.forEach { it.set(AdditionalOffence::event, event) }
        event.courtAppearances.forEach { it.set(CourtAppearance::event, event) }
        event.disposal?.set(Disposal::event, event)
    }

    val EVENT_NON_APP_LENGTH_UNIT: Event = Event(
        id = id(),
        person = PERSON,
        number = "2",
        convictionDate = LocalDate.of(2023, 1, 1),
        mainOffence = MainOffence(
            id = id(),
            date = LocalDate.of(2022, 2, 3),
            count = 1,
            offence = OFFENCE
        ),
        additionalOffences = listOf(
            AdditionalOffence(
                id = id(),
                date = null,
                count = 3,
                offence = OFFENCE
            )
        ),
        courtAppearances = listOf(
            CourtAppearance(
                id = id(),
                date = ZonedDateTime.of(LocalDate.of(2023, 2, 3), LocalTime.of(10, 0, 0), EuropeLondon),
                court = COURT,
                type = COURT_APPEARANCE_TYPE,
                plea = COURT_APPEARANCE_PLEA
            )
        ),
        disposal = Disposal(
            id = id(),
            type = DISPOSAL_TYPE,
            date = LocalDate.of(2023, 3, 4),
            length = 6,
            lengthUnits = LENGTH_UNIT_NA
        )
    ).also { event ->
        event.mainOffence.set(MainOffence::event, event)
        event.additionalOffences.forEach { it.set(AdditionalOffence::event, event) }
        event.courtAppearances.forEach { it.set(CourtAppearance::event, event) }
        event.disposal?.set(Disposal::event, event)
    }
}
