package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.OrderManager
import kotlin.random.Random

object OrderManagerGenerator {
    fun generate(
        event: Event
    ): OrderManager = OrderManager(
        IdGenerator.getAndIncrement(),
        event.id,
        Random.nextLong(),
        Random.nextLong(),
        Random.nextLong()
    )
}
