package uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.ContactType

interface ContactRepository : JpaRepository<Contact, Long>

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

interface ContactOutcomeRepository : JpaRepository<ContactOutcome, Long> {
    fun findByCode(code: String): ContactOutcome?
}

fun ContactTypeRepository.getByCode(code: String): ContactType = findByCode(code) ?: throw NotFoundException("ContactType", "code", code)

fun ContactOutcomeRepository.getByCode(code: String): ContactOutcome = findByCode(code) ?: throw NotFoundException("ContactOutcome", "code", code)
