package uk.gov.justice.digital.hmpps.integrations.delius.user.staff

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff

interface StaffRepository : JpaRepository<Staff, Long> {
    @Query("select s from Staff s where upper(s.user.username) = upper(:username)")
    fun findStaffByUsername(username: String): Staff?
}

fun StaffRepository.getStaff(username: String) =
    findStaffByUsername(username)
        ?: throw NotFoundException("Staff", "username", username)
