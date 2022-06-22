package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProbationArea

object ProbationAreaGenerator {
    val DEFAULT = ProbationArea(
        IdGenerator.getAndIncrement(),
        "PA1",
        Institution(IdGenerator.getAndIncrement(), NomisCaseNoteGenerator.EXISTING_IN_BOTH.locationId)
    )
}