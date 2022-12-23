package uk.gov.justice.digital.hmpps.integrations.delius.recommendation.provider

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.provider.entity.Staff

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCode(code: String): Staff?
}

fun StaffRepository.getStaff(code: String): Staff =
    findByCode(code) ?: throw NotFoundException("Staff", "code", code)
