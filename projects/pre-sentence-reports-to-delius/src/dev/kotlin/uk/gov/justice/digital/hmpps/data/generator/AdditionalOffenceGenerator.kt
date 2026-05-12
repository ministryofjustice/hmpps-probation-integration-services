package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.entity.OffenceEntity
import java.time.LocalDate

object AdditionalOffenceGenerator {
    val DEFAULT_ADDITIONAL_OFFENCE = AdditionalOffence(
        id = id(),
        date = LocalDate.now(),
        event = EventGenerator.DEFAULT_EVENT,
        offence = OffenceEntity(
            id = id(),
            mainCategoryCode = "1  ",
            mainCategoryDescription = "Main additional offence description",
            subCategoryCode = "1A",
            subCategoryDescription = "Sub additional offence description"
        )
    )
}