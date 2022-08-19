package uk.gov.justice.digital.hmpps.integrations.delius.institution

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface InstitutionRepository : JpaRepository<Institution, Long> {
    fun findByNomisCdeCodeAndSelectableIsTrue(code: String): Institution?
    fun findByNomisCdeCodeAndEstablishmentIsTrueAndSelectableIsTrue(code: String): Institution?
}

fun InstitutionRepository.getByNomisCdeCodeAndSelectableIsTrue(code: String): Institution =
    findByNomisCdeCodeAndSelectableIsTrue(code) ?: throw NotFoundException("Institution", "nomisCdeCode", code)
fun InstitutionRepository.getByNomisCdeCodeAndEstablishmentIsTrueAndSelectableIsTrue(code: String): Institution =
    findByNomisCdeCodeAndEstablishmentIsTrueAndSelectableIsTrue(code) ?: throw NotFoundException("Institution", "nomisCdeCode", code)
