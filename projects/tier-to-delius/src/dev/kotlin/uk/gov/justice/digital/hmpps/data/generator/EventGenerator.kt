package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.CaseEntity

object EventGenerator {
    val DEFAULT = generate(eventNumber = "1")

    fun generate(
        person: CaseEntity = CaseEntityGenerator.DEFAULT,
        eventNumber: String = "1",
        id: Long = IdGenerator.getAndIncrement()
    ) = EventEntity(id, eventNumber, person, null)
}
