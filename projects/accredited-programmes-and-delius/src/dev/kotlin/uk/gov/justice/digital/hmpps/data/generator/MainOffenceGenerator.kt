package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import uk.gov.justice.digital.hmpps.entity.sentence.offence.MainOffence
import uk.gov.justice.digital.hmpps.entity.sentence.offence.OffenceEntity
import uk.gov.justice.digital.hmpps.entity.sentence.offence.OffenceEvent
import uk.gov.justice.digital.hmpps.entity.sentence.offence.OffencePerson
import java.time.LocalDate

object MainOffenceGenerator {
    fun generate(event: Event, offence: OffenceEntity) = MainOffence(
        id = id(),
        date = LocalDate.of(2000, 1, 1),
        event = with(event) {
            OffenceEvent(
                id = id,
                number = number,
                person = OffencePerson(person.id, person.crn, person.softDeleted),
                mainOffence = listOf(),
                additionalOffences = listOf(),
                active = active,
                softDeleted = softDeleted
            )
        },
        offence = offence,
        softDeleted = false
    )
}
