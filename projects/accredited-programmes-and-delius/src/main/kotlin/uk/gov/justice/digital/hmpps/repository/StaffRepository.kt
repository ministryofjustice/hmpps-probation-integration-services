package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCode(code: String): Staff?
}

fun StaffRepository.getByCode(code: String): Staff =
    findByCode(code) ?: throw NotFoundException("Staff", "code", code)