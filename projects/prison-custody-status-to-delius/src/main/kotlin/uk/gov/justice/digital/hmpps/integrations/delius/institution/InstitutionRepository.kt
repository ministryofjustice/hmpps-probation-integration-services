package uk.gov.justice.digital.hmpps.integrations.delius.institution

import org.springframework.data.jpa.repository.JpaRepository

interface InstitutionRepository : JpaRepository<Institution, Long> {
    fun findByNomisCdeCodeAndSelectableIsTrue(code: String): Institution
    fun findByNomisCdeCodeAndEstablishmentIsTrueAndSelectableIsTrue(code: String): Institution
}
