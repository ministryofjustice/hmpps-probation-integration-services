package uk.gov.justice.digital.hmpps.integrations.delius.staff

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCodeAndTeamsId(code: String, teamId: Long): Staff?
}

fun StaffRepository.getByCodeAndTeamsId(code: String, teamId: Long) =
    findByCodeAndTeamsId(code, teamId) ?: throw NotFoundException("Staff", "code", code)
