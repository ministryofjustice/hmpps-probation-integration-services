package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team

object StaffGenerator {
    var DEFAULT = generate(
        "${TeamGenerator.DEFAULT.code}U",
        "Unallocated",
        "Staff",
        listOf(TeamGenerator.DEFAULT)
    )

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
