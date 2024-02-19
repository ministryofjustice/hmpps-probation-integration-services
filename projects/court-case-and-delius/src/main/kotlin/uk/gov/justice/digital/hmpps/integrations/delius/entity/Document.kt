package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "document")
class DocumentEntity(
    @Id
    @Column(name = "document_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "alfresco_document_id")
    val alfrescoId: String,

    @Column
    val primaryKeyId: Long,

    @Column(name = "document_name")
    val name: String,

    @Column(name = "document_type")
    val type: String,

    @Column
    val tableName: String,

    @Column(name = "created_datetime")
    val createdAt: ZonedDateTime,

    @Column
    val createdByUserId: Long = 0,

    @Column
    val lastUpdatedUserId: Long = 0,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

interface Document {
    val alfrescoId: String
    val name: String
    val type: String
    val tableName: String
    val createdAt: Instant?
    val author: String?
    val description: String?
    val eventId: Long?
}

fun Document.relatesToEvent() = eventId != null
fun Document.typeDescription() = when (tableName) {
    "OFFENDER" -> if (type == "PREVIOUS_CONVICTION") "PNC previous convictions" else "Offender related"
    "EVENT" -> if (type == "CPS_PACK") "Crown Prosecution Service case pack" else "Sentence related"
    "COURT_REPORT" -> "Court Report"
    "INSTITUTIONAL_REPORT" -> "Institutional Report"
    "ADDRESSASSESSMENT" -> "Address assessment related document"
    "APPROVED_PREMISES_REFERRAL" -> "Approved premises referral related document"
    "ASSESSMENT" -> "Assessment document"
    "CASE_ALLOCATION" -> "Case allocation document"
    "PERSONALCONTACT" -> "Personal contact related document"
    "REFERRAL" -> "Referral related document"
    "NSI" -> "Non Statutory Intervention related document"
    "PERSONAL_CIRCUMSTANCE" -> "Personal circumstance related document"
    "UPW_APPOINTMENT" -> "Unpaid work appointment document"
    "CONTACT" -> "Contact related document"
    else -> error("Un-mapped document type ($tableName/$type)")
}

interface DocumentRepository : JpaRepository<DocumentEntity, Long> {
    @Query(
        """
        select document.alfresco_document_id as "alfrescoId",
               document.document_name as name,
               document.document_type as type,
               document.table_name as "tableName",
               document.created_datetime as "createdAt",
               case
                 when created_by.user_id is not null then created_by.forename || ' ' || created_by.surname
                 when updated_by.user_id is not null then updated_by.forename || ' ' || updated_by.surname
               end as author,
               case
                 when address_assessment.address_assessment_id is not null 
                      then 'Address assessment on ' || to_char(address_assessment.assessment_date, 'dd/MM/yyyy')
                 when approved_premises_referral.approved_premises_referral_id is not null 
                      then 'Approved premises referral on ' || to_char(approved_premises_referral.referral_date, 'dd/MM/yyyy')
                 when assessment.assessment_id is not null 
                      then 'Assessment for ' || r_assessment_type.description || ' on ' || to_char(assessment.assessment_date, 'dd/MM/yyyy')
                 when contact.contact_id is not null 
                      then 'Contact on ' || to_char(contact.contact_date, 'dd/MM/yyyy') || ' for ' || r_contact_type.description 
                 when court_report.court_report_id is not null 
                      then r_court_report_type.description || ' requested by ' || court.court_name || ' on ' || to_char(court_report.date_requested, 'dd/MM/yyyy') 
                 when institutional_report.institutional_report_id is not null 
                      then institutional_report_type.code_description || ' at ' || r_institution.institution_name || ' requested on ' || to_char(institutional_report.date_requested, 'dd/MM/yyyy') 
                 when nsi.nsi_id is not null 
                      then 'Non Statutory Intervention for ' || r_nsi_type.description || ' on ' || to_char(nsi.referral_date, 'dd/MM/yyyy')
                 when personal_circumstance.personal_circumstance_id is not null 
                      then 'Personal circumstance of ' || r_circumstance_type.code_description || ' started on ' || to_char(personal_circumstance.start_date, 'dd/MM/yyyy')
                 when personal_contact.personal_contact_id is not null 
                      then 'Personal contact of type ' || personal_contact_relationship_type.code_description || ' with ' || personal_contact.relationship
                 when referral.referral_id is not null 
                      then 'Referral for ' || r_referral_type.description || ' on ' || to_char(referral.referral_date, 'dd/MM/yyyy')
                 when upw_appointment.upw_appointment_id is not null 
                      then 'Unpaid work appointment on ' || to_char(upw_appointment.appointment_date, 'dd/MM/yyyy') || ' for ' || upw_project.name
               end as description,
               coalesce(
                       event.event_id,
                       court_appearance.event_id,
                       institutional_report_disposal.event_id,
                       approved_premises_referral.event_id,
                       assessment_referral.event_id,
                       case_allocation.event_id,
                       referral.event_id,
                       upw_appointment_disposal.event_id,
                       contact.event_id,
                       nsi.event_id
               ) as "eventId"
        from document
            -- the following joins are to get the event_id from the related entities, for event-level documents
            left join event                        on document.table_name = 'EVENT' and document.primary_key_id = event.event_id
            left join court_report                 on document.table_name = 'COURT_REPORT' and document.primary_key_id = court_report.court_report_id
               left join court_appearance on court_report.court_appearance_id = court_appearance.court_appearance_id
               left join court on court.court_id = court_appearance.court_id
               left join r_court_report_type on r_court_report_type.court_report_type_id = court_report.court_report_type_id
            left join institutional_report         on document.table_name = 'INSTITUTIONAL_REPORT' and document.primary_key_id = institutional_report.institutional_report_id
               left join custody on institutional_report.custody_id = custody.custody_id
               left join disposal institutional_report_disposal on institutional_report_disposal.disposal_id = custody.disposal_id
               left join r_institution on r_institution.institution_id = institutional_report.institution_id and r_institution.establishment = institutional_report.establishment 
               left join r_standard_reference_list institutional_report_type on institutional_report_type.standard_reference_list_id = institutional_report.institution_report_type_id
            left join approved_premises_referral   on document.table_name = 'APPROVED_PREMISES_REFERRAL' and document.primary_key_id = approved_premises_referral.approved_premises_referral_id
            left join assessment                   on document.table_name = 'ASSESSMENT' and document.primary_key_id = assessment.assessment_id and assessment.referral_id is not null
               left join r_assessment_type on assessment.assessment_type_id = r_assessment_type.assessment_type_id
               left join referral assessment_referral on assessment.referral_id = assessment_referral.referral_id
            left join case_allocation              on document.table_name = 'CASE_ALLOCATION' and document.primary_key_id = case_allocation.case_allocation_id
            left join referral                     on document.table_name = 'REFERRAL' and document.primary_key_id = referral.referral_id
                left join r_referral_type on r_referral_type.referral_type_id = referral.referral_type_id
            left join upw_appointment              on document.table_name = 'UPW_APPOINTMENT' and document.primary_key_id = upw_appointment.upw_appointment_id
               left join upw_details on upw_details.upw_details_id = upw_appointment.upw_details_id
               left join upw_project on upw_project.upw_project_id = upw_appointment.upw_project_id
               left join disposal upw_appointment_disposal on upw_appointment_disposal.disposal_id = upw_details.disposal_id
            left join contact                      on document.table_name = 'CONTACT' and document.primary_key_id = contact.contact_id
                left join r_contact_type on r_contact_type.contact_type_id = contact.contact_type_id
            left join nsi                          on document.table_name = 'NSI' and document.primary_key_id = nsi.nsi_id
                left join r_nsi_type on r_nsi_type.nsi_type_id = nsi.nsi_type_id
            -- the following joins are to get extra info for the description of offender-level docs
            left join address_assessment           on document.table_name = 'ADDRESSASSESSMENT' and document.primary_key_id = address_assessment.address_assessment_id
            left join personal_circumstance        on document.table_name = 'PERSONAL_CIRCUMSTANCE' and document.primary_key_id = personal_circumstance.personal_circumstance_id
                left join r_circumstance_type on r_circumstance_type.circumstance_type_id = personal_circumstance.circumstance_type_id
            left join personal_contact             on document.table_name = 'PERSONALCONTACT' and document.primary_key_id = personal_contact.personal_contact_id
                left join r_standard_reference_list personal_contact_relationship_type on personal_contact_relationship_type.standard_reference_list_id = personal_contact.relationship_type_id
            -- the following joins are to populate the author field
            left join user_ created_by on created_by.user_id = document.created_by_user_id
            left join user_ updated_by on updated_by.user_id = document.last_updated_user_id
        where document.offender_id = :personId
          and document.alfresco_document_id is not null
          and document.soft_deleted = 0
          and document.table_name in ('OFFENDER', 'ADDRESSASSESSMENT', 'PERSONALCONTACT', 'PERSONAL_CIRCUMSTANCE',
                                      'EVENT', 'COURT_REPORT', 'INSTITUTIONAL_REPORT', 'APPROVED_PREMISES_REFERRAL', 'ASSESSMENT', 'CASE_ALLOCATION', 'REFERRAL', 'UPW_APPOINTMENT',
                                      'CONTACT', 'NSI')
        """,
        nativeQuery = true
    )
    fun getPersonAndEventDocuments(personId: Long): List<Document>

    @Query("select d.name from DocumentEntity d where d.alfrescoId = :alfrescoId")
    fun findNameByAlfrescoId(alfrescoId: String): String?
}
