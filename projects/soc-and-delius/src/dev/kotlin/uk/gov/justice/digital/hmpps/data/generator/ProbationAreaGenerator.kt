package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Borough
import uk.gov.justice.digital.hmpps.entity.District
import uk.gov.justice.digital.hmpps.entity.ProbationAreaEntity

object ProbationAreaGenerator {
    val DEFAULT_PA = ProbationAreaEntity(
        true,
        "NPS North East",
        "N02",
        null,
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
    val DEFAULT_LDU2 = District(
        true,
        "D02",
        "Durham2",
        DEFAULT_BOROUGH,
        IdGenerator.getAndIncrement()
    )
    val NON_SELECTABLE_PA = ProbationAreaEntity(
        false,
        "NPS North West",
        "N03",
        null,
        IdGenerator.getAndIncrement()
    )
    val NON_SELECTABLE_BOROUGH = Borough(
        true,
        IdGenerator.getAndIncrement(),
        NON_SELECTABLE_PA,
        "N03"
    )
    val NON_SELECTABLE_LDU = District(
        true,
        "D02",
        "Manchester",
        NON_SELECTABLE_BOROUGH,
        IdGenerator.getAndIncrement()
    )
}
