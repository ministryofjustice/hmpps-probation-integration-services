package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventEntity
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.person.CaseEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object EventGenerator {
    val DEFAULT = generate(eventNumber = "1")
    val RECALLED = generate(
        eventNumber = "2",
        mainOffence = Offence(id(), "00200", "Other main offence", false)
    )

    fun generate(
        person: CaseEntity = CaseEntityGenerator.DEFAULT,
        eventNumber: String = "1",
        id: Long = id(),
        mainOffence: Offence = Offence(id(), "00100", "Main offence", true),
        additionalOffences: List<AdditionalOffence> = listOf(
            AdditionalOffence(id(), offence = Offence(id(), "00300", "Additional offence", true)),
            AdditionalOffence(id(), offence = Offence(id(), "00400", "Additional offence without exclusion flag")),
        ),
    ) = EventEntity(
        id, eventNumber, person,
        mainOffence = MainOffence(id(), offence = mainOffence, softDeleted = false),
        additionalOffences = additionalOffences
    )

    fun generate(
        person: Person,
        eventNumber: String = "1",
        id: Long = id()
    ) = generate(
        person = CaseEntity(
            id = person.id,
            crn = person.crn,
            gender = ReferenceDataGenerator.GENDER_MALE,
            tier = null,
        ),
        eventNumber = eventNumber,
        id = id
    )
}
