package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.MainOffence
import uk.gov.justice.digital.hmpps.entity.OffenceEntity

object MainOffenceGenerator {
    val DEFAULT_MAIN_OFFENCE = MainOffence(
        id = IdGenerator.getAndIncrement(),
        eventId = EventGenerator.DEFAULT_EVENT.eventId,
        person = PersonGenerator.DEFAULT_PERSON,
        offence = offenceEntity("Theft")
    )

    val MISSING_COURT_APPEARANCE_MAIN_OFFENCE = MainOffence(
        id = IdGenerator.getAndIncrement(),
        eventId = EventGenerator.MISSING_COURT_APPEARANCE_EVENT.eventId,
        person = PersonGenerator.DEFAULT_PERSON,
        offence = offenceEntity("Theft")
    )

    val MISSING_DISPOSAL_MAIN_OFFENCE = MainOffence(
        id = IdGenerator.getAndIncrement(),
        eventId = EventGenerator.MISSING_DISPOSAL_EVENT.eventId,
        person = PersonGenerator.DEFAULT_PERSON,
        offence = offenceEntity("Theft")
    )

    fun offenceEntity(offenceDescription: String) = OffenceEntity(
        offenceId = IdGenerator.getAndIncrement(),
        mainCategoryCode = "101",
        mainCategoryDescription = offenceDescription,
        subCategoryCode = "01",
        subCategoryDescription = offenceDescription + "-SUB",
        softDeleted = false
    )
}