package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.contact.enforcement.Enforcement
import uk.gov.justice.digital.hmpps.entity.contact.enforcement.EnforcementAction
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy

interface EnforcementRepository : JpaRepository<Enforcement, Long> {
    fun existsByContactId(contactId: Long): Boolean
}

interface EnforcementActionRepository : JpaRepository<EnforcementAction, Long> {
    fun findByCode(code: String): EnforcementAction?
}

fun EnforcementActionRepository.getByCode(code: String) =
    findByCode(code).orNotFoundBy("code", code)
