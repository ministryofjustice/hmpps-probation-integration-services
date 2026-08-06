package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.CaseEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object EventGenerator {
    val DEFAULT = generate(eventNumber = "1")
    val RECALLED = generate(eventNumber = "2")

    fun generate(
        person: CaseEntity = CaseEntityGenerator.DEFAULT,
        eventNumber: String = "1",
        id: Long = IdGenerator.getAndIncrement()
    ) = EventEntity(id, eventNumber, person, null)

    fun generate(
        person: Person,
        eventNumber: String = "1",
        id: Long = IdGenerator.getAndIncrement()
    ) = EventEntity(
        id, eventNumber, CaseEntity(
            id = person.id,
            crn = person.crn,
            gender = ReferenceDataGenerator.GENDER_MALE,
            tier = null,
        ), null
    )
}
