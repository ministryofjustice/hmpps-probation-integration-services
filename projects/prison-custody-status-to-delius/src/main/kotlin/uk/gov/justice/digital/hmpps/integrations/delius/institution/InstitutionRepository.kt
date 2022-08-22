package uk.gov.justice.digital.hmpps.integrations.delius.institution

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface InstitutionRepository : JpaRepository<Institution, InstitutionId> {
    fun findByNomisCdeCodeAndSelectableIsTrue(code: String): Institution?
    fun findByNomisCdeCodeAndIdEstablishmentIsTrueAndSelectableIsTrue(code: String): Institution?
    fun findByCodeAndSelectableIsTrue(code: String): Institution?
}

fun InstitutionRepository.getByNomisCdeCodeAndSelectableIsTrue(code: String): Institution =
    findByNomisCdeCodeAndSelectableIsTrue(code) ?: throw NotFoundException("Institution", "nomisCdeCode", code)
fun InstitutionRepository.getByNomisCdeCodeAndIdEstablishmentIsTrueAndSelectableIsTrue(code: String): Institution =
    findByNomisCdeCodeAndIdEstablishmentIsTrueAndSelectableIsTrue(code) ?: throw NotFoundException("Institution", "nomisCdeCode", code)
fun InstitutionRepository.getByCodeAndSelectableIsTrue(code: String): Institution =
    findByCodeAndSelectableIsTrue(code) ?: throw NotFoundException("Institution", "nomisCdeCode", code)
