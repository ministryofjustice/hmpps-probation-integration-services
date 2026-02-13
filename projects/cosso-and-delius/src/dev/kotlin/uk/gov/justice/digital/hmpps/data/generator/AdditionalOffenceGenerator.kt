package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.MainOffenceGenerator.offenceEntity
import uk.gov.justice.digital.hmpps.entity.AdditionalOffence
import java.time.LocalDate

object AdditionalOffenceGenerator {
    val DEFAULT_ADDITIONAL_OFFENCE = AdditionalOffence(
        id = IdGenerator.getAndIncrement(),
        offence = offenceEntity("Shoplifting"),
        eventId = EventGenerator.DEFAULT_EVENT.eventId,
        offenceDate = LocalDate.now().minusDays(180),
        softDeleted = false
    )
}