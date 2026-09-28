package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventEntity
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.flagged.AdditionalOffenceWithSa2026Exclusions
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.flagged.EventWithSa2026Exclusions
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.flagged.MainOffenceWithSa2026Exclusions
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.flagged.OffenceWithSa2026Exclusions
import uk.gov.justice.digital.hmpps.integrations.delius.person.CaseEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object EventGenerator {
    val DEFAULT = generate(eventNumber = "1")
    val RECALLED = generate(
        eventNumber = "2",
        mainOffence = OffenceWithSa2026Exclusions(id(), "00200", "Other main offence", false)
    )

    fun generate(
        person: CaseEntity = CaseEntityGenerator.DEFAULT,
        eventNumber: String = "1",
        id: Long = id(),
        mainOffence: OffenceWithSa2026Exclusions = OffenceWithSa2026Exclusions(id(), "00100", "Main offence", true),
        additionalOffences: List<AdditionalOffenceWithSa2026Exclusions> = listOf(
            AdditionalOffenceWithSa2026Exclusions(
                id(),
                offence = OffenceWithSa2026Exclusions(id(), "00300", "Additional offence", true)
            ),
            AdditionalOffenceWithSa2026Exclusions(
                id(),
                offence = OffenceWithSa2026Exclusions(id(), "00400", "Additional offence without exclusion flag")
            ),
        ),
    ) = EventWithSa2026Exclusions(
        id, eventNumber, person,
        mainOffence = MainOffenceWithSa2026Exclusions(id(), offence = mainOffence, softDeleted = false),
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

    fun EventWithSa2026Exclusions.toEvent() = EventEntity(
        id = id,
        number = number,
        person = person,
        mainOffence = mainOffence.let {
            MainOffence(
                id = it.id,
                offence = it.offence.let { offence ->
                    Offence(
                        id = offence.id,
                        code = offence.code,
                        description = offence.description
                    )
                },
                softDeleted = it.softDeleted
            )
        },
        additionalOffences = additionalOffences.map {
            AdditionalOffence(
                id = it.id,
                offence = it.offence.let { offence ->
                    Offence(
                        id = offence.id,
                        code = offence.code,
                        description = offence.description
                    )
                },
                softDeleted = it.softDeleted
            )
        }
    )
}
