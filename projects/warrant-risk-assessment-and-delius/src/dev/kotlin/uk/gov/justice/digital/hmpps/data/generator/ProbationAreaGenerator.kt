package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ProbationArea

object ProbationAreaGenerator {
    val DEFAULT_PROBATION_AREA = ProbationArea(
        id = IdGenerator.getAndIncrement(),
        code = "B01",
        description = "probationAreaDescription",
    )
    // This is the "home area" code that will be in LDAP
    val HOME_PROBATION_AREA = ProbationArea(
        id = IdGenerator.getAndIncrement(),
        code = "N01",
        description = "N01 Probation Area",
    )
}
