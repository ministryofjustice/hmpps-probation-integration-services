package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote

interface CaseNoteRepository : JpaRepository<CaseNote, Long> {
    @EntityGraph(attributePaths = ["type"])
    fun findByNomisId(nomisId: Long): CaseNote?
    @EntityGraph(attributePaths = ["type"])
    fun findByExternalReference(externalReference: String): CaseNote?
}
