package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.staff.Staff

interface StaffRepository : JpaRepository<Staff, Long> {
    @EntityGraph(attributePaths = ["user", "teams"])
    fun findByUserUsernameIgnoreCase(username: String): Staff?
}