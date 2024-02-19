package uk.gov.justice.digital.hmpps.integrations.delius.staff

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCode(code: String): Staff?
}

fun StaffRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Staff", "officerCode", code)
