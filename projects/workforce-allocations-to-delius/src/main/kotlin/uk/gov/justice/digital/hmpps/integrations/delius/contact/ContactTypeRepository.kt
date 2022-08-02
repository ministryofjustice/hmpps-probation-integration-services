package uk.gov.justice.digital.hmpps.integrations.delius.contact

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.findByCodeOrThrow(code: String): ContactType =
    findByCode(code) ?: throw NotFoundException("Contact Type", "code", code)
