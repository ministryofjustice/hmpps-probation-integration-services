package uk.gov.justice.digital.hmpps.integrations.delius.custody.date.contact

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

const val EDSS = "EDSS"

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.edssType(): ContactType = findByCode(EDSS) ?: throw NotFoundException("ContactType", "code", EDSS)
