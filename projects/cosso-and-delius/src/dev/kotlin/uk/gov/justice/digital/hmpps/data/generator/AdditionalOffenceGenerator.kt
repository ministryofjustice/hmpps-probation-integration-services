package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.MainOffenceGenerator.offenceEntity
import uk.gov.justice.digital.hmpps.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.entity.OffenceEntity
import java.time.LocalDate

object AdditionalOffenceGenerator {
    val DEFAULT_ADDITIONAL_OFFENCE = AdditionalOffence(
        additionalOffenceId = IdGenerator.getAndIncrement(),
        offence = offenceEntity("Shoplifting"),
        eventId = EventGenerator.DEFAULT_EVENT.eventId,
        offenceDate = LocalDate.now().minusDays(180)
    )
}