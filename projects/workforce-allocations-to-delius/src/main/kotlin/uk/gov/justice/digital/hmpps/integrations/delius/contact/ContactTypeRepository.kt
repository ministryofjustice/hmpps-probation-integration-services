package uk.gov.justice.digital.hmpps.integrations.delius.contact

import org.springframework.data.jpa.repository.JpaRepository

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}