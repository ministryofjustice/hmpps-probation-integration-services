package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.Document

interface DocumentRepository : JpaRepository<Document, Long> {
    @Query("select d.name from Document d where d.person.crn = :crn and d.alfrescoId = :alfrescoId")
    fun findNameByPersonCrnAndAlfrescoId(crn: String, alfrescoId: String): String?

    fun findAllByPersonIdAndSoftDeletedIsFalse(personId: Long): List<Document>
}
