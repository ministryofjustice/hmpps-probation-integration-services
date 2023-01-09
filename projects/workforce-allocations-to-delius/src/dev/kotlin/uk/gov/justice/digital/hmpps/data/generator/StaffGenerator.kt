package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team

object StaffGenerator {
    val DEFAULT = generate(
        "${TeamGenerator.DEFAULT.code}U",
        "Unallocated",
        "Staff",
        listOf(TeamGenerator.DEFAULT)
    )

    var BRIAN_JONES = generate("N02ABS1", "Brian", "Jones", listOf(TeamGenerator.ALLOCATION_TEAM))

    fun generate(
        code: String,
        forename: String,
        surname: String,
        teams: List<Team> = listOf(),
        grade: ReferenceData = ReferenceDataGenerator.PSQ_GRADE,
        id: Long = IdGenerator.getAndIncrement(),
    ): Staff {
        return Staff(
            id = id,
            code = code,
            forename = forename,
            surname = surname,
            grade = grade,
            teams = teams,
        )
    }
}
