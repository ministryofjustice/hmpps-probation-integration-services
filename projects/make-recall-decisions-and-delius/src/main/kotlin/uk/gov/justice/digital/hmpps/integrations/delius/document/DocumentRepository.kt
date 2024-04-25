package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.Document
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocumentEntity

interface DocumentRepository : JpaRepository<DocumentEntity, Long> {
    @Query("select d.name from DocumentEntity d where d.person.crn = :crn and d.alfrescoId = :alfrescoId")
    fun findNameByPersonCrnAndAlfrescoId(crn: String, alfrescoId: String): String?

    @Query(
        """
        select document.alfresco_document_id as "alfrescoId",
               document.document_name as name,
               document.document_type as type,
               document.table_name as "tableName",
               document.created_datetime as "createdAt",
               document.last_saved as "lastUpdatedAt",
               case
                when created_by.user_id is not null then created_by.forename || ' ' || created_by.surname
                when updated_by.user_id is not null then updated_by.forename || ' ' || updated_by.surname
               end as author,
               'NAT AP Residence Plan on ' || to_char(document.created_datetime, 'dd/MM/yyyy') as description
        from document
        join offender on offender.offender_id = document.offender_id
        left join user_ created_by on created_by.user_id = document.created_by_user_id
        left join user_ updated_by on updated_by.user_id = document.last_updated_user_id
        where offender.crn = :crn
        and document.table_name = 'CONTACT'
        and document.template_name in ('NAT AP Residence Plan - Male', 'NAT AP Residence Plan - Female')
        and document.alfresco_document_id is not null
        and document.soft_deleted = 0
        order by created_datetime desc
        """,
        nativeQuery = true
    )
    fun findAPResidencePlanDocument(crn: String, pageable: Pageable = PageRequest.of(0, 1)): List<Document>
}

fun DocumentRepository.getAPResidencePlanDocument(crn: String) = findAPResidencePlanDocument(crn).firstOrNull() ?: throw NotFoundException("AP Residence Plan Document", "crn", crn)
