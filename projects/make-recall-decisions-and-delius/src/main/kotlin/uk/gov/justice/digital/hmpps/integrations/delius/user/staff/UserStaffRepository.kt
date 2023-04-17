package uk.gov.justice.digital.hmpps.integrations.delius.user.staff

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff

interface UserStaffRepository : JpaRepository<Staff, Long> {
    @Query("select s.code from Staff s where lower(s.user.username) = lower(:username)")
    fun findUserStaffCode(username: String): String?
}
