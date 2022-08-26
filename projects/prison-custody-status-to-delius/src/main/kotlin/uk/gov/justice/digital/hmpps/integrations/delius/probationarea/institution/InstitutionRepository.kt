package uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface InstitutionRepository : JpaRepository<Institution, InstitutionId> {
    fun findByNomisCdeCode(code: String): Institution?
    fun findByNomisCdeCodeAndIdEstablishmentIsTrue(code: String): Institution?
    fun findByCode(code: String): Institution?
}

fun InstitutionRepository.getByNomisCdeCode(code: String): Institution =
    findByNomisCdeCode(code) ?: throw NotFoundException("Institution", "nomisCdeCode", code)
fun InstitutionRepository.getByNomisCdeCodeAndIdEstablishmentIsTrue(code: String): Institution =
    findByNomisCdeCodeAndIdEstablishmentIsTrue(code) ?: throw NotFoundException("Institution", "nomisCdeCode", code)
fun InstitutionRepository.getByCode(code: String): Institution =
    findByCode(code) ?: throw NotFoundException("Institution", "code", code)
