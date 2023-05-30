package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.casedetails.entity.Event

object EventGenerator {
    val DEFAULT = generate(1234567890L)

    fun generate(
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(id, CaseGenerator.DEFAULT.id, true, "1")
}
