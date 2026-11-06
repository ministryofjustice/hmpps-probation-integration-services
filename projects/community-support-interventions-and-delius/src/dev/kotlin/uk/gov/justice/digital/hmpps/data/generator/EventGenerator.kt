package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Event

object EventIdGenerator {
    val EVENT_ID1 = IdGenerator.getAndIncrement()
    val EVENT_ID2 = IdGenerator.getAndIncrement()
    val EVENT_ID3 = IdGenerator.getAndIncrement()
}
object EventGenerator {
    val EVENT = Event(
        id = EventIdGenerator.EVENT_ID1,
        person = PersonGenerator.PERSON1,
        mainOffence = MainOffenceGenerator.MAIN_OFFENCE1,
        active = true,
        softDeleted = false
    )

    val EVENT2 = Event(
        id = EventIdGenerator.EVENT_ID2,
        person = PersonGenerator.PERSON2,
        mainOffence = MainOffenceGenerator.MAIN_OFFENCE2,
        active = true,
        softDeleted = false
    )

    val EVENT3 = Event(
        id = EventIdGenerator.EVENT_ID3,
        person = PersonGenerator.PERSON1,
        mainOffence = MainOffenceGenerator.MAIN_OFFENCE1,
        active = true,
        softDeleted = false
    )
}