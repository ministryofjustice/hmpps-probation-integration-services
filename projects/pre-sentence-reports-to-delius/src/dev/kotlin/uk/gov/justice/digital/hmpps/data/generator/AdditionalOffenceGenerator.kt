package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.entity.OffenceEntity
import java.time.LocalDate

object AdditionalOffenceGenerator {
    val DEFAULT_ADDITIONAL_OFFENCE = AdditionalOffence(
        additionalOffenceId = IdGenerator.getAndIncrement(),
        offenceDate = LocalDate.now(),
        eventId = EventGenerator.DEFAULT_EVENT.id,
        offence = OffenceEntity(
            id = IdGenerator.getAndIncrement(),
            mainCategoryCode = "1  ",
            mainCategoryDescription = "Main additional offence description",
            subCategoryCode = "1A",
            subCategoryDescription = "Sub additional offence description"
        )
    )
}