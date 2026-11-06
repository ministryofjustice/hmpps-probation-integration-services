package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.MainOffence

object MainOffenceGenerator {
    val MAIN_OFFENCE1 = MainOffence(
        id = IdGenerator.getAndIncrement(),
        offence = OffenceGenerator.OFFENCE1,
        softDeleted = false,
        eventId = EventGenerator.EVENT.id
    )

    val MAIN_OFFENCE2 = MainOffence(
        id = IdGenerator.getAndIncrement(),
        offence = OffenceGenerator.OFFENCE1,
        softDeleted = false,
        eventId = EventGenerator.EVENT2.id
    )
}

object OffenceGenerator {
    val OFFENCE1 = uk.gov.justice.digital.hmpps.entity.Offence(
        id = IdGenerator.getAndIncrement(),
        mainCategoryCode = "AA1",
        mainCategoryDescription = "Assault",
        subCategoryCode = "AA",
        subCategoryDescription = "Assault with intent to cause serious harm"
    )
}