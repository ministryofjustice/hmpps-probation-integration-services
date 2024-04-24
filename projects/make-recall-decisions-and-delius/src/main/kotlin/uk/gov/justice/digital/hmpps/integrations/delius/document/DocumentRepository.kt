package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
               'Approved premises referral on ' || to_char(approved_premises_referral.referral_date, 'dd/MM/yyyy') as description,
               approved_premises_referral.event_id as "eventId"
        from document
        join offender on offender.offender_id = document.offender_id
        join approved_premises_referral on document.table_name = 'APPROVED_PREMISES_REFERRAL' and document.primary_key_id = approved_premises_referral.approved_premises_referral_id
        left join user_ created_by on created_by.user_id = document.created_by_user_id
        left join user_ updated_by on updated_by.user_id = document.last_updated_user_id
        where offender.crn = :crn
        and document.alfresco_document_id is not null
        and document.soft_deleted = 0
        """,
        nativeQuery = true
    )
    fun getApprovedPremisesDocuments(crn: String): List<Document>
}
