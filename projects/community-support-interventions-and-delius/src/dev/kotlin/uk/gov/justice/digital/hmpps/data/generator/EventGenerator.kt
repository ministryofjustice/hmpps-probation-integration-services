package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Event

object EventGenerator {
    val EVENT = Event(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PERSON1,
        active = true,
        softDeleted = false
    )

    val EVENT2 = Event(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PERSON2,
        active = true,
        softDeleted = false
    )

    val EVENT3 = Event(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.PERSON1,
        active = true,
        softDeleted = false
    )
}