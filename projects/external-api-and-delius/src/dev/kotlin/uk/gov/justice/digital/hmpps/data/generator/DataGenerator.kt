package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

object DataGenerator {
    val DEFAULT_PROVIDER = Provider("DEF", "Default Provider", IdGenerator.getAndIncrement())
    val DEFAULT_TEAM =
        Team("DEFUAT", "Default Team", "020 334 1257", "team@justice.co.uk", IdGenerator.getAndIncrement())
    val JOHN_SMITH = Staff("DEFJOSM", "John", "Smith", null, IdGenerator.getAndIncrement())
    val JS_USER = StaffUser(JOHN_SMITH, "john-smith", IdGenerator.getAndIncrement())

    val PERSON = Person(id = IdGenerator.getAndIncrement(), crn = "A000001", softDeleted = false)
    val PERSON_MANAGER = PersonManager(
        PERSON,
        DEFAULT_PROVIDER,
        DEFAULT_TEAM,
        JOHN_SMITH,
        true,
        IdGenerator.getAndIncrement()
    )

    val OFFENCE = Offence(
        id = IdGenerator.getAndIncrement(),
        code = "12345",
        description = "Test offence",
        mainCategoryCode = "123",
        mainCategoryDescription = "Test",
        subCategoryCode = "45",
        subCategoryDescription = "offence",
        schedule15SexualOffence = true,
        schedule15ViolentOffence = null
    )

    val COURT = Court(id = IdGenerator.getAndIncrement(), name = "Manchester Crown Court")
    val COURT_APPEARANCE_TYPE =
        ReferenceData(id = IdGenerator.getAndIncrement(), code = "SEN", description = "Sentence")
    val COURT_APPEARANCE_PLEA =
        ReferenceData(id = IdGenerator.getAndIncrement(), code = "GLT", description = "Not guilty")
    val DISPOSAL_TYPE =
        DisposalType(id = IdGenerator.getAndIncrement(), description = "ORA Suspended Sentence Order", "SC")
    val MONTHS = ReferenceData(id = IdGenerator.getAndIncrement(), code = "MNTH", description = "Months")

    val EVENT: Event

    init {
        EVENT = Event(
            id = IdGenerator.getAndIncrement(),
            person = PERSON,
            number = "1",
            convictionDate = LocalDate.of(2023, 1, 2),
            mainOffence = MainOffence(
                id = IdGenerator.getAndIncrement(),
                date = LocalDate.of(2023, 1, 1),
                count = 1,
                offence = OFFENCE
            ),
            additionalOffences = listOf(
                AdditionalOffence(
                    id = IdGenerator.getAndIncrement(),
                    date = null,
                    count = 3,
                    offence = OFFENCE
                )
            ),
            courtAppearances = listOf(
                CourtAppearance(
                    id = IdGenerator.getAndIncrement(),
                    date = ZonedDateTime.of(LocalDate.of(2023, 2, 3), LocalTime.of(10, 0, 0), EuropeLondon),
                    court = COURT,
                    type = COURT_APPEARANCE_TYPE,
                    plea = COURT_APPEARANCE_PLEA
                )
            ),
            disposal = Disposal(
                id = IdGenerator.getAndIncrement(),
                type = DISPOSAL_TYPE,
                date = LocalDate.of(2023, 3, 4),
                length = 6,
                lengthUnits = MONTHS
            )
        )
        EVENT.mainOffence.set(MainOffence::event, EVENT)
        EVENT.additionalOffences.forEach { it.set(AdditionalOffence::event, EVENT) }
        EVENT.courtAppearances.forEach { it.set(CourtAppearance::event, EVENT) }
        EVENT.disposal?.set(Disposal::event, EVENT)
    }
}
