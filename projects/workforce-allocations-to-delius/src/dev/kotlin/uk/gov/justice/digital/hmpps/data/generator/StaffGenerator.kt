package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import uk.gov.justice.digital.hmpps.integrations.delius.user.StaffUser
import uk.gov.justice.digital.hmpps.set

object StaffGenerator {
    val DEFAULT = generate("${TeamGenerator.DEFAULT.code}U", "Unallocated", "Staff", listOf(TeamGenerator.DEFAULT))
    val STAFF_WITH_USER = generate(
        "${TeamGenerator.ALLOCATION_TEAM.code}1",
        "Joe",
        "Bloggs",
        listOf(TeamGenerator.ALLOCATION_TEAM),
        StaffUserGenerator.DEFAULT
    )

    fun generate(
        code: String,
        forename: String,
        surname: String,
        teams: List<Team> = listOf(),
        user: StaffUser? = null,
        grade: ReferenceData = ReferenceDataGenerator.PSQ_GRADE,
        id: Long = IdGenerator.getAndIncrement(),
    ): Staff {
        val staff = Staff(
            id = id,
            code = code,
            forename = forename,
            surname = surname,
            grade = grade,
            user = user,
            teams = teams,
        )
        user?.set("staff", staff)
        return staff
    }
}
