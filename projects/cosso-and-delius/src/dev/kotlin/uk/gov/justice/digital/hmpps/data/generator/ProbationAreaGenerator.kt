package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ProbationArea

object ProbationAreaGenerator {
    val DEFAULT_PROBATION_AREA = ProbationArea(IdGenerator.getAndIncrement(), "N02", "DEFAULT_PROBATION_AREA")
}