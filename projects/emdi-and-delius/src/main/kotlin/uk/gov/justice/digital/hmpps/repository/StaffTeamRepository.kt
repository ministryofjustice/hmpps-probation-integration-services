package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.staff.StaffTeam
import uk.gov.justice.digital.hmpps.entity.staff.StaffTeamId

interface StaffTeamRepository : JpaRepository<StaffTeam, StaffTeamId> {
    /**
     * Find all teams with full hierarchy (team/lau/pdu/provider)
     */
    @Query(
        """
        select st from StaffTeam st
        join fetch st.team t
        join fetch t.localAdminUnit lau
        join fetch lau.probationDeliveryUnit pdu
        join fetch pdu.provider p
        where upper(st.user.distinguishedName) = upper(:username)
        order by p.code, pdu.code, lau.code, t.code
        """
    )
    fun findTeamsWithHierarchyByUsername(username: String): List<StaffTeam>
}

