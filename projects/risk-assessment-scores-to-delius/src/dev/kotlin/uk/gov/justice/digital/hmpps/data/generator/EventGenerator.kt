package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.NULL_EVENT_PROCESSING
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import java.time.ZonedDateTime

object EventGenerator {
    val DEFAULT = generate(eventNumber = "1")
    val NEP_1 = generate(NULL_EVENT_PROCESSING, "2", createdDateTime = ZonedDateTime.now().minusMonths(1))
    val NEP_2 = generate(NULL_EVENT_PROCESSING, "3", createdDateTime = ZonedDateTime.now().minusDays(1))
    val NEP_3 = generate(NULL_EVENT_PROCESSING, "4", createdDateTime = ZonedDateTime.now().minusDays(1))
    val MERGED_TO = generate(PersonGenerator.MERGED_TO)

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        eventNumber: String = "1",
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true,
        softDeleted: Boolean = false,
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
    ) = Event(id, eventNumber, person, active = active, softDeleted = softDeleted, createdDateTime = createdDateTime)
}
