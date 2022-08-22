package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.manager.OrderManager
import kotlin.random.Random

object OrderManagerGenerator {
    fun generate(
        event: Event
    ): OrderManager = OrderManager(
        IdGenerator.getAndIncrement(),
        event.id,
        Random.nextLong(),
        Random.nextLong(),
        Random.nextLong(),
    )
}
