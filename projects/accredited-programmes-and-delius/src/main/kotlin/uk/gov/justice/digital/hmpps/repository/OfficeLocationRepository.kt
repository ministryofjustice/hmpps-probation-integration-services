package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface OfficeLocationRepository : JpaRepository<OfficeLocation, Long> {
    fun findByCode(code: String): OfficeLocation?
}

fun OfficeLocationRepository.getByCode(code: String): OfficeLocation =
    findByCode(code) ?: throw NotFoundException("OfficeLocation", "code", code)