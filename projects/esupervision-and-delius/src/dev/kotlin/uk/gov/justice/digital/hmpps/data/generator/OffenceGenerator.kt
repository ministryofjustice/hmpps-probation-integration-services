package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.event.offence.Offence

object OffenceGenerator {
    val BURGLARY = Offence(
        id = id(),
        code = "03100",
        description = "Aggravated burglary in a building other than a dwelling (including attempts)"
    )
}
