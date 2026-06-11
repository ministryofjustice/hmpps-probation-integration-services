package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ProbationArea

object ProbationAreaGenerator {
    val DEFAULT_PROBATION_AREA = ProbationArea(IdGenerator.getAndIncrement(), "N02", "DEFAULT_PROBATION_AREA")
    val PROBATION_AREA_N01 = ProbationArea(IdGenerator.getAndIncrement(), "N01", "N01 Probation Area")
}