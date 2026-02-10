package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.MainOffence
import uk.gov.justice.digital.hmpps.entity.Offence
import java.time.LocalDate

object MainOffenceGenerator {
    val DEFAULT_MAIN_OFFENCE = MainOffence(
        id = IdGenerator.getAndIncrement(),
        date = LocalDate.now(),
        event = EventGenerator.DEFAULT_EVENT,
        person = PersonGenerator.DEFAULT_PERSON,
        offence = Offence(
            id = IdGenerator.getAndIncrement(),
            mainCategoryCode = "1",
            mainCategoryDescription = "Main offence description",
            subCategoryCode = "1A",
            subCategoryDescription = "Sub offence description"
        )
    )
}