package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import java.time.ZonedDateTime

object BusinessInteractionGenerator {
    val INSERT_PERSON = BusinessInteraction(
        IdGenerator.getAndIncrement(),
        BusinessInteractionCode.INSERT_PERSON.code,
        ZonedDateTime.now().minusMonths(6)
    )
    val INSERT_ADDRESS = BusinessInteraction(
        IdGenerator.getAndIncrement(),
        BusinessInteractionCode.INSERT_ADDRESS.code,
        ZonedDateTime.now().minusMonths(6)
    )
    val INSERT_EVENT = BusinessInteraction(
        IdGenerator.getAndIncrement(),
        BusinessInteractionCode.INSERT_EVENT.code,
        ZonedDateTime.now().minusMonths(6)
    )
    val INSERT_COURT_APPEARANCE = BusinessInteraction(
        IdGenerator.getAndIncrement(),
        BusinessInteractionCode.INSERT_COURT_APPEARANCE.code,
        ZonedDateTime.now().minusMonths(6)
    )
}
