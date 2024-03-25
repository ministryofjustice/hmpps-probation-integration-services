package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.StaffEntity

interface StaffRepository : JpaRepository<StaffEntity, Long> {
    @EntityGraph(attributePaths = ["user", "teams.district.borough"])
    fun findByCode(code: String): StaffEntity?

    @Query(
        """
            select s from StaffEntity s
            join fetch s.user u
            left join fetch s.teams t
            left join fetch t.district d
            left join fetch d.borough
            where upper(u.username) = upper(:username)
        """
    )
    fun findByUserUsername(username: String): StaffEntity?

    @EntityGraph(attributePaths = ["user", "teams.district.borough"])
    fun findStaffById(id: Long): StaffEntity?
}
