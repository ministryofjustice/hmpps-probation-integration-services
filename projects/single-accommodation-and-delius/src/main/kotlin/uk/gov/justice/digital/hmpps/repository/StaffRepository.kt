package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.staff.Staff

interface StaffRepository : JpaRepository<Staff, Long> {
    @EntityGraph(attributePaths = ["user", "teams"])
    fun findByUserUsernameIgnoreCase(username: String): Staff?

    @EntityGraph(attributePaths = ["user"])
    @Query(
        value = "select distinct s from Staff s join s.teams t where t.code in :teamCodes",
        countQuery = "select count(distinct s.id) from Staff s join s.teams t where t.code in :teamCodes"
    )
    fun findByTeamCodeIn(teamCodes: Collection<String>, pageable: Pageable): Page<Staff>
}