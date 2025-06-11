package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "document")
@SQLRestriction("soft_deleted = 0")
class Document(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "alfresco_document_id")
    val alfrescoId: String,

    @Column(name = "document_name")
    var name: String,

    @Column
    val primaryKeyId: Long,

    @Column
    val tableName: String,

    val externalReference: String,

    var lastSaved: ZonedDateTime? = null,

    var lastUpdatedUserId: Long? = null,

    @Column(columnDefinition = "char")
    var workInProgress: String,

    @Column(columnDefinition = "char")
    var status: String,

    @Version
    var rowVersion: Long = 0,

    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "document_id")
    val id: Long,
) {
    companion object {
        fun breachNoticeUrn(uuid: UUID): String =
            "urn:hmpps:breach-notice:$uuid"
    }
}

interface DocumentRepository : JpaRepository<Document, Long> {
    @Query(
        """
        select coalesce(event.event_id,
                        court_appearance.event_id,
                        institutional_report_disposal.event_id,
                        approved_premises_referral.event_id,
                        assessment_referral.event_id,
                        case_allocation.event_id, 
                        referral.event_id,
                        upw_appointment_disposal.event_id, 
                        contact.event_id,
                        nsi.event_id)
        from document
            -- the following joins are to get the event_id from the related entities, for event-level documents
        left join event on document.table_name = 'EVENT' and document.primary_key_id = event.event_id
        left join court_report
                  on document.table_name = 'COURT_REPORT' and document.primary_key_id = court_report.court_report_id
        left join court_appearance on court_report.court_appearance_id = court_appearance.court_appearance_id
        left join institutional_report on document.table_name = 'INSTITUTIONAL_REPORT' and
                                          document.primary_key_id = institutional_report.institutional_report_id
        left join custody on institutional_report.custody_id = custody.custody_id
        left join disposal institutional_report_disposal on institutional_report_disposal.disposal_id = custody.disposal_id
        left join approved_premises_referral on document.table_name = 'APPROVED_PREMISES_REFERRAL' and document.primary_key_id =
                                                                                                       approved_premises_referral.approved_premises_referral_id
        left join assessment on document.table_name = 'ASSESSMENT' and document.primary_key_id = assessment.assessment_id and
                                assessment.referral_id is not null
        left join referral assessment_referral on assessment.referral_id = assessment_referral.referral_id
        left join case_allocation
                  on document.table_name = 'CASE_ALLOCATION' and document.primary_key_id = case_allocation.case_allocation_id
        left join referral on document.table_name = 'REFERRAL' and document.primary_key_id = referral.referral_id
        left join upw_appointment
                  on document.table_name = 'UPW_APPOINTMENT' and document.primary_key_id = upw_appointment.upw_appointment_id
        left join upw_details on upw_details.upw_details_id = upw_appointment.upw_details_id
        left join disposal upw_appointment_disposal on upw_appointment_disposal.disposal_id = upw_details.disposal_id
        left join contact on document.table_name = 'CONTACT' and document.primary_key_id = contact.contact_id
        left join nsi on document.table_name = 'NSI' and document.primary_key_id = nsi.nsi_id
        where document.external_reference = :urn
        """,
        nativeQuery = true
    )
    fun findEventIdFromDocument(urn: String): Long?

    fun findByExternalReference(urn: String): Document?

    fun existsByTableNameAndPrimaryKeyIdAndIdNot(tableName: String, primaryKeyId: Long, id: Long): Boolean
}

fun DocumentRepository.eventId(urn: String): Long = findEventIdFromDocument(urn)
    ?: throw NotFoundException("Event", "breach notice urn", urn)