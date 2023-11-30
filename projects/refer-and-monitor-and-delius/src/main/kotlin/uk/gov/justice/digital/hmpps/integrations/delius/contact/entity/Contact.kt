package uk.gov.justice.digital.hmpps.integrations.delius.contact.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
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
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.extensions.hasChanged
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "contact")
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
class Contact(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "contact_date")
    var date: LocalDate = LocalDate.now(),

    @Column(name = "contact_start_time")
    var startTime: ZonedDateTime,

    @Column(name = "contact_end_time")
    var endTime: ZonedDateTime? = null,

    @Column(name = "attended")
    @Convert(converter = YesNoConverter::class)
    var attended: Boolean? = null,

    @Column(name = "complied")
    @Convert(converter = YesNoConverter::class)
    var complied: Boolean? = null,

    @Column(name = "latest_enforcement_action_id")
    var enforcementActionId: Long? = null,

    var enforcement: Boolean? = null,

    val eventId: Long? = null,

    val nsiId: Long? = null,

    @Column(name = "probation_area_id")
    val providerId: Long,
    val teamId: Long,
    val staffId: Long,

    @Column(name = "office_location_id")
    var locationId: Long? = null,

    @Column(name = "rar_activity", length = 1)
    @Convert(converter = YesNoConverter::class)
    var rarActivity: Boolean? = null,

    val linkedContactId: Long? = null,

    var externalReference: String? = null,

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
) {
    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    var outcome: ContactOutcome? = null
        set(value) {
            field = value
            attended = value?.attendance
            complied = value?.compliantAcceptable
            creditHours()
        }

    val duration: Duration
        get() =
            if (endTime != null) {
                Duration.between(startTime, endTime)
            } else {
                Duration.ZERO
            }

    @Lob
    @Column
    var notes: String? = null
        private set

    @Column(name = "hours_credited", columnDefinition = "number")
    var hoursCredited: Double? = null
        private set

    fun addNotes(notes: String?): Contact {
        this.notes = (this.notes ?: "") + """${System.lineSeparator()}
            |$notes
        """.trimMargin()
        return this
    }

    private fun creditHours() {
        hoursCredited = if (outcome?.code == ContactOutcome.Code.COMPLIED.value && duration > Duration.ZERO) {
            BigDecimal.valueOf(duration.toMinutes())
                .divide(BigDecimal(60), 2, RoundingMode.HALF_UP)
                .toDouble()
        } else {
            null
        }
    }

    fun replaceIfRescheduled(
        externalReference: String,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ): Contact? =
        if (startTime.hasChanged(this.startTime) || endTime.hasChanged(this.endTime)) {
            Contact(
                person,
                type,
                startTime.toLocalDate(),
                startTime,
                endTime,
                eventId = eventId,
                nsiId = nsiId,
                providerId = providerId,
                teamId = teamId,
                staffId = staffId,
                locationId = locationId,
                rarActivity = rarActivity,
                externalReference = externalReference
            )
        } else {
            this.externalReference = externalReference
            null
        }
}

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    val code: String,
    @Column(name = "national_standards_contact", length = 1)
    @Convert(converter = YesNoConverter::class)
    val nationalStandards: Boolean,
    @Column(name = "attendance_contact")
    @Convert(converter = YesNoConverter::class)
    val attendanceContact: Boolean,
    @Id
    @Column(name = "contact_type_id")
    val id: Long
) {
    enum class Code(val value: String, val rar: Boolean = false) {
        REFER_TO_PERSON_MANAGER("AROM"),
        REVIEW_ENFORCEMENT_STATUS("ARWS"),
        CRSAPT("CRSAPT", true),
        CRSSAA("CRSSAA"),
        CRSNOTE("CRSNOTE"),
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
class ContactOutcome(
    val code: String,

    @Column(name = "outcome_attendance", length = 1)
    @Convert(converter = YesNoConverter::class)
    var attendance: Boolean?,

    @Column(name = "outcome_compliant_acceptable", length = 1)
    @Convert(converter = YesNoConverter::class)
    var compliantAcceptable: Boolean?,

    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long
) {
    enum class Code(val value: String) {
        COMPLIED("ATTC"),
        FAILED_TO_COMPLY("AFTC"),
        FAILED_TO_ATTEND("AFTA"),
        RESCHEDULED_SERVICE_REQUEST("RSSR"),
        WITHDRAWN("APPW")
    }
}

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "enforcement")
@SQLRestriction("soft_deleted = 0")
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
    val actionTakenDate: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "action_taken_time")
    val actionTakenTime: ZonedDateTime = ZonedDateTime.now(),

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
    val description: String,
    val responseByPeriod: Long?,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val contactType: ContactType,

    @Id
    @Column(name = "enforcement_action_id")
    val id: Long = 0
) {
    enum class Code(val value: String) {
        REFER_TO_PERSON_MANAGER("ROM")
    }
}
