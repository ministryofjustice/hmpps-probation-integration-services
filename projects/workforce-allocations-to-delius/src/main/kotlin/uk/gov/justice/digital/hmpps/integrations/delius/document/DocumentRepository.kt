package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.Document

interface DocumentRepository : JpaRepository<Document, Long> {

    fun findAllByPersonId(personId: Long): List<Document>
}