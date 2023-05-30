package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Borough
import uk.gov.justice.digital.hmpps.entity.District
import uk.gov.justice.digital.hmpps.entity.ProbationAreaEntity

object ProbationAreaGenerator {
    val DEFAULT_PA = ProbationAreaEntity(
        true,
        "NPS North East",
        "N02",
        IdGenerator.getAndIncrement()
    )
    val DEFAULT_BOROUGH = Borough(
        true,
        IdGenerator.getAndIncrement(),
        DEFAULT_PA,
        "N02"
    )
    val DEFAULT_LDU = District(
        true,
        "D01",
        "Durham",
        DEFAULT_BOROUGH,
        IdGenerator.getAndIncrement()
    )
}
