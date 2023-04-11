package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.entity.CaseEntity
import uk.gov.justice.digital.hmpps.controller.entity.EventEntity

object EventGenerator {
    val DEFAULT = generate(eventNumber = "1")

    fun generate(
        person: CaseEntity = CaseEntityGenerator.DEFAULT,
        eventNumber: String = "1",
        id: Long = IdGenerator.getAndIncrement(),
    ) = EventEntity(id, eventNumber, person, null)
}
