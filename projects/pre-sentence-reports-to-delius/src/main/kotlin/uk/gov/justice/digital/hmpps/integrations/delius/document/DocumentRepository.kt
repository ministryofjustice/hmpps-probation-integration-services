package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DocumentRepository : JpaRepository<Document, Long> {
    @Query(
        """
      SELECT d FROM Document d 
      WHERE lower(substring(d.externalReference, -36, 36)) = lower(:uuid)
      AND d.softDeleted = false
  """,
    )
    fun findByExternalReference(uuid: String): Document?
}
