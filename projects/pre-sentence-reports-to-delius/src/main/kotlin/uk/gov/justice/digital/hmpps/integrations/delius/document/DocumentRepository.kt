package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.data.jpa.repository.JpaRepository

interface DocumentRepository : JpaRepository<Document, Long> {
    fun findByExternalReference(externalReference: String): Document?
}
