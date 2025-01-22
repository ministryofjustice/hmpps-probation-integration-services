package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote

interface CaseNoteRepository : JpaRepository<CaseNote, Long> {
    fun findByNomisId(nomisId: Long): CaseNote?
    fun findByExternalReference(externalReference: String): CaseNote?
}
