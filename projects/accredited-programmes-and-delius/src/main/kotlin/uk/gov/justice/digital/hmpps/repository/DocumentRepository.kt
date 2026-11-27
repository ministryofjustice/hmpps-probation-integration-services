package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.document.Document

interface DocumentRepository : JpaRepository<Document, Long>
