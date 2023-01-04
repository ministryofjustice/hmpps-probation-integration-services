package uk.gov.justice.digital.hmpps.integrations.delius.provider

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCode(code: String): Staff?

    @Query(
        """
        select case when count(t) > 0 then true else false end
        from Staff s 
        left join fetch Team t
        where s.id = :staffId
        and t.id = :teamId
        """
    )
    fun verifyTeamMembership(staffId: Long, teamId: Long): Boolean

    fun findAllByTeamsCode(teamCode: String): List<Staff>
}
