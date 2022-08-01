package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import java.time.ZonedDateTime

object BusinessInteractionGenerator {
    val ADD_PERSON_ALLOCATION = BusinessInteraction(
        IdGenerator.getAndIncrement(),
        BusinessInteractionCode.ADD_PERSON_ALLOCATION.code,
        ZonedDateTime.now().minusMonths(6)
    )
    val ADD_EVENT_ALLOCATION = BusinessInteraction(
        IdGenerator.getAndIncrement(),
        BusinessInteractionCode.ADD_EVENT_ALLOCATION.code,
        ZonedDateTime.now().minusMonths(6)
    )
    val CREATE_COMPONENT_TRANSFER = BusinessInteraction(
        IdGenerator.getAndIncrement(),
        BusinessInteractionCode.CREATE_COMPONENT_TRANSFER.code,
        ZonedDateTime.now().minusMonths(6)
    )
}
