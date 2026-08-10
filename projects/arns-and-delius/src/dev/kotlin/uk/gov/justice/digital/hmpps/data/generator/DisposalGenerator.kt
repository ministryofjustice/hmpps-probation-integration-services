package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Disposal
import uk.gov.justice.digital.hmpps.entity.Event
import java.time.LocalDate

object DisposalGenerator {

    fun generate(
        event: Event,
        startDate: LocalDate,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Disposal(
        id = id,
        event = event,
        startDate = startDate,
        softDeleted = softDeleted,
    )
}