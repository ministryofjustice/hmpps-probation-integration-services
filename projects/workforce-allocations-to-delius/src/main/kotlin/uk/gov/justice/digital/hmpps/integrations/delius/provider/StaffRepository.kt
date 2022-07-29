package uk.gov.justice.digital.hmpps.integrations.delius.provider

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCode(code: String): Staff?

    @Query(
        """
        select case when count(st) > 0 then true else false end
        from StaffTeam st 
        where st.staffId = :staffId
        and st.teamId = :teamId
        """
    )
    fun verifyTeamMembership(staffId: Long, teamId: Long): Boolean
}
