package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType

interface CaseNoteTypeRepository : JpaRepository<CaseNoteType, Long> {
    fun findByCode(code: String): CaseNoteType?
}

fun CaseNoteTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Case note type", "code", code)