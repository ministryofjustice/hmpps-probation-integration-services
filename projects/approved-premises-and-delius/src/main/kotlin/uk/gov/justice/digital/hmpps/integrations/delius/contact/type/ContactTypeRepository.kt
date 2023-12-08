package uk.gov.justice.digital.hmpps.integrations.delius.contact.type

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String): ContactType = findByCode(code) ?: throw NotFoundException("ContactType", "code", code)
