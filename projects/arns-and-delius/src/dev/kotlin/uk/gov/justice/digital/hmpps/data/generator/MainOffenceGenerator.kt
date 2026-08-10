package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Event
import uk.gov.justice.digital.hmpps.entity.MainOffence
import uk.gov.justice.digital.hmpps.entity.Offence

object MainOffenceGenerator {

    fun generate(
        event: Event,
        offence: Offence,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = MainOffence(
        id = id,
        offence = offence,
        event = event,
        softDeleted = softDeleted,
    )
}