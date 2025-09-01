package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.staff.Provider
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ProviderRepository : JpaRepository<Provider, Long> {
    fun findByCode(code: String): Provider?
}

fun ProviderRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Provider", "code", code)