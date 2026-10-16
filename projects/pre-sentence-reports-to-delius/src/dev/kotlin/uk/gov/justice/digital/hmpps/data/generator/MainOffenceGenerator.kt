package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.MainOffence
import uk.gov.justice.digital.hmpps.entity.OffenceEntity
import java.time.LocalDate

object MainOffenceGenerator {
    val DEFAULT_MAIN_OFFENCE = MainOffence(
        id = id(),
        date = LocalDate.now(),
        event = EventGenerator.DEFAULT_EVENT,
        person = PersonGenerator.DEFAULT_PERSON,
        offence = OffenceEntity(
            id = id(),
            code = "12345",
            description = "Full description",
            mainCategoryCode = "123",
            mainCategoryDescription = "Main additional offence description",
            subCategoryCode = "45",
            subCategoryDescription = "Sub additional offence description"
        )
    )
}