package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.io.Serializable
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "contact")
@SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
@SQLRestriction("soft_deleted = 0")
class Contact(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_generator")
    @Column(name = "contact_id")
    val id: Long? = null,

    @Column
    val linkedContactId: Long? = null,

    @Column(name = "contact_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime,

    @Column(name = "contact_end_time")
    val endTime: ZonedDateTime?,

    @Column(name = "rqmnt_id")
    val rqmntId: Long? = null,

    @Column(name = "lic_condition_id")
    val licConditionId: Long? = null,

    @Column(name = "provider_location_id")
    val providerLocationId: Long? = null,

    @Column(name = "provider_employee_id")
    val providerEmployeeId: Long? = null,

    @Column(name = "hours_credited", columnDefinition = "NUMBER(10,2)")
    var hoursCredited: Double? = null,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    @Column(name = "visor_contact")
    @Convert(converter = YesNoConverter::class)
    val visorContact: Boolean? = false,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team? = null,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @Column(name = "visor_exported")
    @Convert(converter = YesNoConverter::class)
    val visorExported: Boolean? = false,

    @Column
    val partitionAreaId: Long = 0L,

    @Column
    val officeLocationId: Long? = null,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "alert_active")
    val alert: Boolean? = false,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "attended")
    val attended: Boolean? = false,

    @CreatedDate
    @Column(nullable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Convert(converter = YesNoConverter::class)
    @Column(name = "complied")
    val complied: Boolean? = false,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "sensitive")
    val sensitive: Boolean? = false,

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "event_id")
    val eventId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column
    val providerTeamId: Long? = null,

    @Column
    val contactOutcomeTypeId: Long? = null,

    @Column
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column
    val explanationId: Long? = null,

    @Column(name = "last_updated_user_id")
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(name = "trust_provider_flag", columnDefinition = "number")
    val trustProviderFlag: Boolean = false,

    @Column
    val staffEmployeeId: Long,

    @Column
    val probationAreaId: Long? = null,

    @Column
    val trustProviderTeamId: Long? = null,

    @Column(name = "enforcement", columnDefinition = "number")
    val enforcement: Boolean? = null,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "document_linked")
    val documentLinked: Boolean? = null,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "upload_linked")
    val uploadLinked: Boolean? = null,

    @Column(name = "latest_enforcement_action_id")
    val latestEnforcementActionId: Long? = null,

    @Column
    val nsiId: Long? = null,

    @Column
    val tableName: String? = null,

    @Column
    val primaryKeyId: Long? = null,

    @Column
    val pssRqmntId: Long? = null,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "rar_activity")
    val rarActivity: Boolean? = null,

    @Column
    val nomisCaseNoteId: Long? = null,

    @Column
    val linkedDocumentContactId: Long? = null,

    @Column(name = "description")
    val description: String? = null,

    @Column
    val externalReference: String? = null,

    @Column
    val rarLinkedContactId: Long? = null,

    @Column
    val sessionDeliveredConTypeId: Long? = null,
)

interface ContactRepository : JpaRepository<Contact, Long>

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    @Column
    val code: String,

    @Column(name = "attendance_contact")
    @Convert(converter = YesNoConverter::class)
    val attendanceContact: Boolean = false,

    @Column
    val description: String,

    @OneToMany(mappedBy = "id.contactTypeId")
    val categories: List<ContactCategory> = emptyList(),

    @Column(name = "sgc_flag", columnDefinition = "number")
    val systemGenerated: Boolean = false,

    @Column(name = "national_standards_contact")
    @Convert(converter = YesNoConverter::class)
    val nationalStandardsContact: Boolean = false,
)

enum class ContactTypeCode(val code: String) {
    COURT_APPEARANCE("EAPP"),
}

@Immutable
@Entity
@Table(name = "r_contact_typecontact_category")
class ContactCategory(
    @EmbeddedId
    val id: ContactCategoryId,
)

@Embeddable
class ContactCategoryId(
    @Column(name = "contact_type_id")
    val contactTypeId: Long,

    @ManyToOne
    @JoinColumn(name = "standard_reference_list_id")
    val category: ReferenceData,
) : Serializable

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,

    val code: String,

    val description: String,

    @Column(name = "outcome_attendance")
    @Convert(converter = YesNoConverter::class)
    val outcomeAttendance: Boolean? = null,

    @Column(name = "outcome_compliant_acceptable")
    @Convert(converter = YesNoConverter::class)
    val outcomeCompliantAcceptable: Boolean? = null,
)

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Contact", "code", code)