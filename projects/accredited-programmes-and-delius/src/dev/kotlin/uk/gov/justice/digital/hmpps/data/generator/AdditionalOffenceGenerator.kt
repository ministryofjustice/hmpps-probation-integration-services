package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.toCrn
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import uk.gov.justice.digital.hmpps.entity.sentence.offence.AdditionalOffence
import uk.gov.justice.digital.hmpps.entity.sentence.offence.OffenceEntity
import uk.gov.justice.digital.hmpps.entity.sentence.offence.OffenceEvent
import java.time.LocalDate

object AdditionalOffenceGenerator {
    fun generate(event: Event, offence: OffenceEntity) = AdditionalOffence(
        id = id(),
        date = LocalDate.of(2000, 1, 1),
        event = with(event) {
            OffenceEvent(
                id = id,
                number = number,
                person = person.toCrn(),
                mainOffence = listOf(),
                additionalOffences = listOf(),
                active = active,
                softDeleted = softDeleted
            )
        },
        offenceEntity = offence,
        softDeleted = false
    )
}
