package uk.gov.justice.digital.hmpps.integrations.delius.appointment

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "contact")
@SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
@SQLRestriction("soft_deleted = 0")
class Appointment(
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: AppointmentPerson,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: AppointmentType,

    @Column(name = "contact_date")
    var date: LocalDate,

    @Column(name = "contact_start_time")
    var startTime: ZonedDateTime?,

    @Column(name = "contact_end_time")
    var endTime: ZonedDateTime?,

    @ManyToOne
    @JoinColumn(name = "team_id")
    var team: AppointmentTeam,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    var staff: AppointmentStaff,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    var provider: AppointmentProvider,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    var location: AppointmentLocation?,

    notes: String?,

    sensitive: Boolean?,

    rarActivity: Boolean?,

    outcome: AppointmentOutcome?,

    val externalReference: String?,

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_generator")
    @Column(name = "contact_id")
    val id: Long = 0
) {
    var partitionAreaId: Long = 0

    @Version
    @Column(name = "row_version")
    val version: Long = 0

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdByUserId: Long? = null

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastUpdatedUserId: Long? = null

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    var outcome: AppointmentOutcome? = outcome
        private set

    @Column(name = "rar_activity", length = 1)
    @Convert(converter = YesNoConverter::class)
    var rarActivity: Boolean? = rarActivity
        private set

    @Lob
    var notes: String? = notes
        private set

    fun appendNotes(parts: List<String>) =
        appendNotes(*parts.toTypedArray())

    fun appendNotes(vararg extraNotes: String) = apply {
        val lineBreak = System.lineSeparator() + System.lineSeparator()
        val appendable = extraNotes.filter { !it.isBlank() }
        if (appendable.isNotEmpty()) {
            val prefix = notes?.let { notes + lineBreak } ?: ""
            notes = appendable.joinToString(separator = lineBreak, prefix = prefix)
        }
    }

    @Column(name = "sensitive")
    @Convert(converter = YesNoConverter::class)
    var sensitive: Boolean? = sensitive
        private set

    fun amendmentSensitive(sensitive: Boolean) {
        this.sensitive = this.sensitive == true || sensitive
    }

    fun applyOutcome(outcome: AppointmentOutcome) = apply {
        this.outcome = outcome
        if (outcome.attendance != true) {
            rarActivity = false
        }
        // TODO handle non-compliant outcomes - current use case only includes compliant reschedule outcome
    }

    fun isInTheFuture(): Boolean {
        val today = LocalDate.now()
        return date.isAfter(today) ||
            (date.isEqual(today) && startTime?.toLocalTime()?.isAfter(LocalTime.now()) == true)
    }

    companion object {
        const val URN_PREFIX = "urn:uk:gov:hmpps:manage-supervision-service:appointment:"
    }
}

@Immutable
@Entity
@Table(name = "r_contact_type")
class AppointmentType(
    @Column
    val code: String,

    val description: String,

    @Column(name = "attendance_contact")
    @Convert(converter = YesNoConverter::class)
    val attendanceContact: Boolean,

    @Id
    @Column(name = "contact_type_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class AppointmentOutcome(
    val code: String,
    val description: String,

    @Column(name = "outcome_attendance")
    @Convert(converter = YesNoConverter::class)
    val attendance: Boolean? = null,

    @Column(name = "outcome_compliant_acceptable")
    @Convert(converter = YesNoConverter::class)
    val acceptable: Boolean? = null,

    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,
) {
    enum class Code(val value: String) {
        RESCHEDULED_SERVICE("RSSR"),
        RESCHEDULED_POP("RSOF"),
    }
}

interface AppointmentOutcomeRepository : JpaRepository<AppointmentOutcome, Long> {
    fun findByCode(code: String): AppointmentOutcome?
}

fun AppointmentOutcomeRepository.getByCode(code: String): AppointmentOutcome =
    findByCode(code) ?: throw NotFoundException("Outcome", "code", code)

interface AppointmentRepository : JpaRepository<Appointment, Long> {
    @Query(
        """
            select count(a)
            from Appointment a
            where a.person.id = :personId 
                and a.type.attendanceContact = true
                and a.date = :date
                and to_char(a.startTime, 'HH24:MI') < to_char(:endTime, 'HH24:MI') 
                and to_char(a.endTime, 'HH24:MI') > to_char(:startTime, 'HH24:MI')
                and a.outcome is null
                and a.id <> coalesce(:appointmentId, 0) 
        """,
    )
    fun countClashes(
        personId: Long,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        appointmentId: Long?,
    ): Int

    @Query(
        """
        select a from Appointment a
        where a.person.crn = :crn
        and a.type.attendanceContact = true
        and a.outcome is null
        and (
            a.date < current_date or (a.date = current_date and to_char(a.endTime, 'HH24:MI') < to_char(current_timestamp, 'HH24:MI'))
        )
    """
    )
    fun findOverdueOutcomes(crn: String): List<Appointment>
}

fun AppointmentRepository.getAppointment(id: Long) =
    findByIdOrNull(id) ?: throw NotFoundException("Appointment", "id", id)

fun AppointmentRepository.appointmentClashes(
    personId: Long,
    date: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime,
    appointmentId: Long?,
) = countClashes(personId, date, startTime, endTime, appointmentId) > 0