package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Event
import uk.gov.justice.digital.hmpps.entity.Person
import java.time.LocalDate

object EventGenerator {
    fun generate(
        person: Person,
        convictionDate: LocalDate? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Event(
        id = id,
        person = person,
        convictionDate = convictionDate,
        softDeleted = softDeleted,
    )
}