package uk.gov.justice.digital.hmpps.integrations.delius.contact.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "contact")
@Where(clause = "soft_deleted = 0")
@SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
class Contact(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "contact_date")
    val date: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime = date,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    var outcome: AppointmentOutcome? = null,

    @Lob
    @Column
    var notes: String? = null,

    @Column(name = "latest_enforcement_action_id")
    var enforcementActionId: Long? = null,

    var enforcement: Boolean? = null,

    val eventId: Long? = null,

    @Column(name = "rqmnt_id")
    val requirementId: Long? = null,

    val nsiId: Long? = null,

    @Column(name = "probation_area_id")
    val providerId: Long,
    val teamId: Long,
    val staffId: Long,

    @Id
    @Column(name = "contact_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_generator")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(nullable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    val partitionAreaId: Long = 0
)

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    val code: String,
    @Id
    @Column(name = "contact_type_id")
    val id: Long
) {
    enum class Code(val value: String, val rar: Boolean = false) {
        CRSAPT("CRSAPT", true),
        CRSSAA("CRSSAA"),
        NSI_REFERRAL("NREF"),
        NSI_COMMENCED("NCOM"),
        NSI_TERMINATED("NTER"),
        IN_PROGRESS("C091"),
        COMPLETED("C092")
    }
}

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class AppointmentOutcome(
    val code: String,
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long
) {
    enum class Code(val value: String) {
        COMPLIED("ATTC"),
        FAILED_TO_COMPLY("AFTC"),
        FAILED_TO_ATTEND("AFTA")
    }
}

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "enforcement")
@Where(clause = "soft_deleted = 0")
class Enforcement(

    @ManyToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    @ManyToOne
    @JoinColumn(name = "enforcement_action_id")
    val action: EnforcementAction? = null,

    @Column(name = "response_date")
    val responseDate: ZonedDateTime? = null,

    @Column(name = "action_taken_date")
    val actionTakenDate: ZonedDateTime? = null,

    @Column(name = "action_taken_time")
    val actionTakenTime: ZonedDateTime? = null,

    @Id
    @SequenceGenerator(name = "enforcement_id_seq", sequenceName = "enforcement_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "enforcement_id_seq")
    @Column(name = "enforcement_id")
    val id: Long = 0,

    @Column(name = "partition_area_id")
    val partitionAreaId: Long = 0,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(name = "row_version")
    @Version
    val version: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0
)

@Immutable
@Entity
@Table(name = "r_enforcement_action")
class EnforcementAction(
    val code: String,
    val responseByPeriod: Long?,

    @Id
    @Column(name = "enforcement_action_id")
    val id: Long = 0
) {
    enum class Code(val value: String) {
        REFER_TO_PERSON_MANAGER("ROM")
    }
}
