package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.StaffEntity

interface StaffRepository : JpaRepository<StaffEntity, Long> {
    @EntityGraph(attributePaths = ["user", "teams.district.borough"])
    fun findByCode(code: String): StaffEntity?

    @EntityGraph(attributePaths = ["user", "teams.district.borough"])
    fun findByUserUsername(username: String): StaffEntity?

    @EntityGraph(attributePaths = ["user", "teams.district.borough"])
    fun findStaffById(id: Long): StaffEntity?
}
