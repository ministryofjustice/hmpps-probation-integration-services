package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.LocalAdminUnit
import uk.gov.justice.digital.hmpps.entity.staff.Team

object TeamGenerator {
    val DEFAULT = generate(code = "T001", description = "Default Team", lau = LocalAdminUnitGenerator.DEFAULT)
    val SECOND = generate(code = "T002", description = "Second Team", lau = LocalAdminUnitGenerator.DEFAULT)
    val THIRD = generate(code = "T003", description = "Third Team", lau = LocalAdminUnitGenerator.SECOND)
    val LONDON_TEAM = generate(code = "T_LON", description = "London Team", lau = LocalAdminUnitGenerator.LONDON)

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        lau: LocalAdminUnit
    ) = Team(id, code, description, lau)
}

