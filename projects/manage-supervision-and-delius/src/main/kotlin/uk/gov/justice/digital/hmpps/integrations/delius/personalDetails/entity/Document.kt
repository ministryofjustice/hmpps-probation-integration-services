package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.*
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import uk.gov.justice.digital.hmpps.service.DocumentLevelCode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "document")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "table_name", discriminatorType = DiscriminatorType.STRING)
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "document_id_generator", sequenceName = "document_id_seq", allocationSize = 1)
abstract class Document {
    @Id
    @GeneratedId(generator = "document_id_generator")
    @Column(name = "document_id")
    open var id: Long = 0

    @Column(name = "offender_id")
    open var personId: Long = 0

    @Column(name = "primary_key_id")
    open var primaryKeyId: Long? = null

    @Column(name = "alfresco_document_id")
    open var alfrescoId: String = ""

    @Column(name = "document_name")
    open var name: String = ""

    @Column(name = "document_type")
    open var type: String = ""

    @Column(name = "created_datetime")
    open var createdAt: ZonedDateTime? = ZonedDateTime.now()

    @Column(name = "last_saved")
    open var lastUpdated: ZonedDateTime = ZonedDateTime.now()

    @Column(name = "created_by_user_id")
    open var createdByUserId: Long? = 0

    @Column(name = "last_updated_user_id")
    open var lastUpdatedUserId: Long? = 0

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    open var sensitive: Boolean = false

    @Convert(converter = YesNoConverter::class)
    open var workInProgress: Boolean? = null

    @Column(columnDefinition = "number")
    open var softDeleted: Boolean = false

    @Convert(converter = YesNoConverter::class)
    open var status: Boolean = true

    open var partitionAreaId: Long = 0
}

@Entity
@Subselect(
    """
        select document.offender_id as offender_id,
               document.alfresco_document_id as alfresco_id,
               document.document_name as name,
               document.created_datetime as created_at,
               document.last_saved as last_updated_at,
               document.work_in_progress as work_in_progress,
               case
                 when document.sensitive = 1
                    then 'Sensitive'
                 else
                   null
                 end as status,
               case
                 when document.table_name = 'OFFENDER' and document.document_type = 'DOCUMENT'
                   then 'Person'
                 when document.table_name = 'OFFENDER' and document.document_type = 'PREVIOUS_CONVICTION'
                   then 'Pre Cons'  
                 when document.table_name = 'ADDRESSASSESSMENT'
                   then 'Address assessment'
                 when document.table_name = 'PERSONALCONTACT'
                   then 'Personal contact'
                 when document.table_name = 'PERSONAL_CIRCUMSTANCE'
                   then 'Personal circumstance'
                 when document.table_name = 'EVENT' and document.document_type = 'DOCUMENT'
                   then 'Event'
                 when document.table_name = 'EVENT' and document.document_type = 'CPS_PACK'
                   then 'CPS Pack'  
                 when document.table_name = 'COURT_REPORT'
                   then 'Court report'
                 when document.table_name = 'INSTITUTIONAL_REPORT'
                   then 'Institutional report'
                 when document.table_name = 'APPROVED_PREMISES_REFERRAL'
                   then 'AP Referral'
                 when document.table_name = 'ASSESSMENT'
                   then 'Assessment'
                 when document.table_name = 'CASE_ALLOCATION'
                   then 'Case allocation'
                 when document.table_name = 'REFERRAL'
                   then 'Referral'       
                 when document.table_name = 'UPW_APPOINTMENT'
                   then 'UPW Appointment'        
                 when document.table_name = 'CONTACT'
                   then 'Contact'
                 when document.table_name = 'NSI'
                   then 'NSI'
                 when document.table_name = 'OFFENDER_ADDRESS'
                   then 'Address'          
                 when document.table_name = 'EQUALITY'
                   then 'Person'         
                 when document.table_name = 'DRUGS_TEST'
                   then 'Event'       
                 when document.table_name = 'REGISTRATION'
                   then 'Register'         
               end as doc_level,    
               case
                 when document.table_name = 'OFFENDER' and document.document_type = 'PREVIOUS_CONVICTION'
                   then 'Pre Cons'
                 when document.table_name = 'EVENT' and document.document_type = 'CPS_PACK'
                   then 'CPS Pack'    
                 when address_assessment.address_assessment_id is not null 
                      then 
                        case 
                        when offender_address.address_number is not null 
                        then 
                            offender_address.address_number || ' ' || offender_address.street_name
                        else
                            offender_address.street_name
                        end
                 when approved_premises_referral.approved_premises_referral_id is not null 
                      then 'Referred ' || to_char(approved_premises_referral.referral_date, 'dd/MM/yyyy')
                 when assessment.assessment_id is not null 
                      then r_assessment_type.description
                 when contact.contact_id is not null 
                      then r_contact_type.description 
                 when court_report.court_report_id is not null 
                      then r_court_report_type.description
                 when institutional_report.institutional_report_id is not null 
                      then institutional_report_type.code_description
                 when nsi.nsi_id is not null 
                      then
                          case
                            when nsi_sub_type.code_description is not null
                              then r_nsi_type.description || ' - ' || nsi_sub_type.code_description
                            else
                              r_nsi_type.description
                            end  
                 when personal_circumstance.personal_circumstance_id is not null 
                      then r_circumstance_type.code_description || ' - ' || r_circumstance_sub_type.code_description
                 when personal_contact.personal_contact_id is not null 
                      then personal_contact.first_name || ' ' || personal_contact.surname
                 when referral.referral_id is not null 
                      then r_referral_type.description
                 when upw_appointment.upw_appointment_id is not null 
                      then to_char(upw_appointment.appointment_date, 'dd/MM/yyyy') 
                 when offender_address.address_type_id is not null 
                      then 'Address'
                 when document.table_name = 'EQUALITY'
                      then 'Equality monitoring'
                 when document.table_name = 'DRUGS_TEST'
                      then 'Drug test'     
                 when registration.register_type_id is not null     
                      then r_register_type.description
                 when disposal.disposal_id is not null
                      then r_disposal_type.description
                 when document.table_name = 'CASE_ALLOCATION'
                      then 'Case allocation'
                 when document.table_name = 'OFFENDER_ADDRESS'
                      then 'Address'         
                 else
                  'Person'
               end as type,
               case
                 when created_by.user_id is not null then created_by.forename || ' ' || created_by.surname
                 when updated_by.user_id is not null then updated_by.forename || ' ' || updated_by.surname
               end as author
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
            left join assessment                   on document.table_name = 'ASSESSMENT' and document.primary_key_id = assessment.assessment_id
               left join r_assessment_type on assessment.assessment_type_id = r_assessment_type.assessment_type_id
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
                left join r_standard_reference_list nsi_sub_type on nsi_sub_type.standard_reference_list_id = nsi.nsi_sub_type_id
            left join address_assessment           on document.table_name = 'ADDRESSASSESSMENT' and document.primary_key_id = address_assessment.address_assessment_id
                 left join offender_address on offender_address.offender_address_id = address_assessment.offender_address_id
            left join personal_circumstance        on document.table_name = 'PERSONAL_CIRCUMSTANCE' and document.primary_key_id = personal_circumstance.personal_circumstance_id
                left join r_circumstance_type on r_circumstance_type.circumstance_type_id = personal_circumstance.circumstance_type_id
                 left join r_circumstance_sub_type on r_circumstance_sub_type.circumstance_sub_type_id = personal_circumstance.circumstance_sub_type_id
            left join personal_contact             on document.table_name = 'PERSONALCONTACT' and document.primary_key_id = personal_contact.personal_contact_id
                left join r_standard_reference_list personal_contact_relationship_type on personal_contact_relationship_type.standard_reference_list_id = personal_contact.relationship_type_id
            left join offender_address oa on document.table_name = 'OFFENDER_ADDRESS' and document.primary_key_id = oa.offender_address_id
                left join r_standard_reference_list address_type on address_type.standard_reference_list_id = oa.address_type_id  
             left join registration on document.table_name = 'REGISTRATION' and document.primary_key_id = registration.registration_id   
                left join r_register_type on r_register_type.register_type_id = registration.register_type_id        
             left join disposal on disposal.event_id = event.event_id
                left join r_disposal_type on r_disposal_type.disposal_type_id = disposal.disposal_type_id      
            -- the following joins are to populate the author field
            left join user_ created_by on created_by.user_id = document.created_by_user_id
            left join user_ updated_by on updated_by.user_id = document.last_updated_user_id
        where document.alfresco_document_id is not null
          and document.soft_deleted = 0
          and document.table_name in ('OFFENDER', 'ADDRESSASSESSMENT', 'PERSONALCONTACT', 'PERSONAL_CIRCUMSTANCE',
                                      'EVENT', 'COURT_REPORT', 'INSTITUTIONAL_REPORT', 'APPROVED_PREMISES_REFERRAL', 'ASSESSMENT', 'CASE_ALLOCATION', 'REFERRAL', 'UPW_APPOINTMENT',
                                      'CONTACT', 'NSI', 'OFFENDER_ADDRESS', 'EQUALITY', 'DRUGS_TEST', 'REGISTRATION')
"""
)
data class DocumentEntity(
    @Id
    @Column(name = "alfresco_id", nullable = false)
    val alfrescoId: String,
    val offenderId: Long,
    val name: String,
    @Column(name = "doc_level")
    val level: String,
    val type: String,
    val createdAt: LocalDateTime?,
    val lastUpdatedAt: LocalDateTime?,
    val author: String?,
    val status: String? = null,
    @Convert(converter = YesNoConverter::class)
    val workInProgress: Boolean? = false
)

interface DocumentsRepository : JpaRepository<DocumentEntity, Long> {
    fun findByOffenderId(offenderId: Long, pageable: Pageable): Page<DocumentEntity>

    @Query(
        """
            select d from DocumentEntity d
            where d.offenderId = :offenderId
            and (:name is null or upper(d.name) like '%' || upper(:name) || '%' ESCAPE '\')
            and ((:createdDateFrom is null or :createdDateTo is null) or (d.createdAt >= :createdDateFrom and d.createdAt <= :createdDateTo))
        """
    )
    fun searchWithFilename(
        offenderId: Long,
        name: String?,
        createdDateFrom: LocalDateTime?,
        createdDateTo: LocalDateTime?,
        pageable: Pageable
    ): Page<DocumentEntity>

    @Query(
        """
            select d from DocumentEntity d
            where d.offenderId = :offenderId
            and ((:createdDateFrom is null or :createdDateTo is null) or (d.createdAt >= :createdDateFrom and d.createdAt <= :createdDateTo))
            and (:#{#documentLevelCode.name} = "ALL" or d.level = :#{#documentLevelCode.description})
            and ((:ids is null and :keywords is null) or ((:ids is not null and d.alfrescoId in (:ids)) or (:keywords is not null and regexp_substr(d.name, :keywords, 1, 1, 'i') is not null )))
        """
    )
    fun search(
        offenderId: Long,
        createdDateFrom: LocalDateTime?,
        createdDateTo: LocalDateTime?,
        documentLevelCode: DocumentLevelCode,
        ids: List<String>? = null,
        keywords: String? = null,
        pageable: Pageable
    ): Page<DocumentEntity>
}

@Entity(name = "Prison")
@Table(name = "r_institution")
class Prison(
    @Id
    @Column(name = "institution_id")
    val id: Long,

    @Column(name = "nomis_cde_code")
    val nomisCode: String,

    @Column(name = "institution_name")
    val institutionName: String? = null,

    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean,
)

@Entity
class Assessment(
    @Id val assessmentId: Long,
    val assessmentTypeId: Long,
    val referralId: Long?,
    val assessmentDate: LocalDate
)

@Entity
class CaseAllocation(@Id val caseAllocationId: Long, val eventId: Long)

@Entity
class AddressAssessment(@Id val addressAssessmentId: Long, val assessmentDate: LocalDate, val offenderAddressId: Long)

@Entity
class UpwProject(@Id val upwProjectId: Long, val name: String)

@Entity
@Table(name = "referral")
class ReferralEntity(@Id val referralId: Long, val referralTypeId: Long, val referralDate: LocalDate, val eventId: Long)

@Entity
@Table(name = "r_referral_type")
class ReferralType(@Id val referralTypeId: Long, val description: String)

@Entity
@Table(name = "r_assessment_type")
class AssessmentType(@Id val assessmentTypeId: Long, val description: String)

@Entity
class ApprovedPremisesReferral(
    @Id
    val approvedPremisesReferralId: Long,
    val eventId: Long,
    val approvedPremisesId: Long?,
    val referralDate: LocalDate,
)

@Entity
class InstitutionalReport(
    @Id
    @Column(name = "institutional_report_id")
    val id: Long,

    @Id
    @Column(name = "institution_id")
    val institutionId: Long,

    @ManyToOne
    @JoinColumn(name = "institution_report_type_id", updatable = false)
    val type: ReferenceData,

    @Column(name = "date_required")
    val dateRequired: LocalDate,

    @Column(name = "date_requested")
    val dateRequested: LocalDate,

    @Column(name = "date_completed")
    val dateCompleted: LocalDate? = null,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "custody_id")
    val custodyId: Long,

    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean,
)

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("CONTACT")
class ContactDocument(
    @JoinColumn(name = "primary_key_id", referencedColumnName = "contact_id", insertable = false, updatable = false)
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val contact: Contact?
) : Document()

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("OFFENDER")
class PersonDocument : Document()

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("EVENT")
class EventDocument : Document()

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("COURT_REPORT")
class CourtReportDocument : Document()

interface CourtDocumentDetails {
    val id: String
    val lastSaved: LocalDateTime
    val documentName: String
}

interface DocumentRepository : JpaRepository<Document, Long> {
    fun findByPersonId(personId: Long): List<PersonDocument>

    @Query("select d.name from Document d join Person p on p.id = d.personId and p.crn = :crn and d.alfrescoId = :alfrescoId")
    fun findNameByPersonCrnAndAlfrescoId(crn: String, alfrescoId: String): String?

    @Query(
        """
            SELECT id, lastSaved, documentName FROM (
                SELECT d.ALFRESCO_DOCUMENT_ID AS id, d.LAST_SAVED AS lastSaved, d.DOCUMENT_NAME AS documentName
                FROM DOCUMENT d 
                JOIN EVENT e 
                ON e.EVENT_ID = d.PRIMARY_KEY_ID 
                WHERE e.EVENT_ID = :id
                AND e.EVENT_NUMBER = :eventNumber
                AND TABLE_NAME = 'EVENT'
                UNION 
                SELECT d.ALFRESCO_DOCUMENT_ID AS id, d.LAST_SAVED AS lastSaved, d.DOCUMENT_NAME AS documentName
                FROM DOCUMENT d 
                JOIN COURT_REPORT cr 
                ON cr.COURT_REPORT_ID = d.PRIMARY_KEY_ID 
                JOIN COURT_APPEARANCE ca 
                ON ca.COURT_APPEARANCE_ID = cr.COURT_APPEARANCE_ID 
                JOIN EVENT e 
                ON e.EVENT_ID = ca.EVENT_ID 
                WHERE e.EVENT_ID = :id
                AND e.EVENT_NUMBER = :eventNumber
                AND d.TABLE_NAME = 'COURT_REPORT'   
            )
            ORDER BY lastSaved DESC 
        """, nativeQuery = true
    )
    fun getCourtDocuments(id: Long, eventNumber: String): List<CourtDocumentDetails>

    fun findByPrimaryKeyId(id: Long): Document?
}

fun DocumentRepository.getDocument(crn: String, alfrescoId: String) =
    findNameByPersonCrnAndAlfrescoId(crn, alfrescoId) ?: throw NotFoundException("Document", "alfrescoId", alfrescoId)
