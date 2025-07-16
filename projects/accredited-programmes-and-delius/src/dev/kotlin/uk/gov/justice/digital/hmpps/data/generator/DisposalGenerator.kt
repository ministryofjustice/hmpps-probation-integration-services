package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.DisposalType
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import java.time.LocalDate

object DisposalGenerator {
    fun generate(event: Event, type: DisposalType, length: Long, lengthUnits: ReferenceData) = Disposal(
        id = id(),
        event = event,
        type = type,
        date = LocalDate.of(2000, 1, 1),
        length = length,
        lengthUnits = lengthUnits,
        expectedEndDate = LocalDate.of(2100, 1, 1),
        enteredExpectedEndDate = null,
        licenceConditions = listOf(),
        requirements = listOf(),
        custody = null,
        active = true,
        softDeleted = false,
    )
}
