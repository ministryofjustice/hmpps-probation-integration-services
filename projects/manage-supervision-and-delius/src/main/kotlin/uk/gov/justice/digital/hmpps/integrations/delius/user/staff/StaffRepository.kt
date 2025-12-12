package uk.gov.justice.digital.hmpps.integrations.delius.user.staff

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.entity.Team

interface StaffRepository : JpaRepository<Staff, Long> {
    @Query("select s.teams from Staff s where s.code = :staffCode")
    fun findTeamsByStaffCode(staffCode: String): List<Team>
}
