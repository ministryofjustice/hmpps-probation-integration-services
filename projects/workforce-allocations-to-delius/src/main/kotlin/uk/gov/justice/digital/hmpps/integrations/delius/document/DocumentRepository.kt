package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.CourtReportDocument
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.Document

interface DocumentRepository : JpaRepository<Document, Long> {

    fun findAllByPersonIdAndSoftDeletedIsFalse(personId: Long): List<Document>

    fun findByAlfrescoIdAndSoftDeletedIsFalse(id: String): Document?

    @Query(
        """
        select crd from CourtReportDocument crd
        join fetch crd.courtReport.type
        where crd.personId = :personId and crd.alfrescoId is not null
        order by crd.lastSaved desc
    """
    )
    fun findLatestCourtReport(personId: Long): CourtReportDocument?

    @Query(
        """
        select d from Document d
        where d.personId = :personId and d.alfrescoId is not null
        and (d.type = 'CPS_PACK' OR d.type = 'PREVIOUS_CONVICTION')
    """
    )
    fun findCpsAndPreCons(personId: Long): List<Document>
}
