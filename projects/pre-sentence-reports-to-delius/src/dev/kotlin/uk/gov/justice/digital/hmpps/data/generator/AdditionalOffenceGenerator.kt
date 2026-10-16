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
            code = "12345",
            description = "Full description",
            mainCategoryCode = "123",
            mainCategoryDescription = "Main additional offence description",
            subCategoryCode = "45",
            subCategoryDescription = "Sub additional offence description"
        )
    )
}