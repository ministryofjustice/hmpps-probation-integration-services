package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.CaseNoteType

interface CaseNoteTypeRepository : JpaRepository<CaseNoteType, Long> {
    fun findByCode(code: String): CaseNoteType?
}
