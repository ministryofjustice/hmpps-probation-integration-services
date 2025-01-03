package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.*
import java.time.format.DateTimeFormatter

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "contact")
@SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
@SQLRestriction("soft_deleted = 0")
class Appointment(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: AppointmentType,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime?,

    @Column(name = "contact_end_time")
    val endTime: ZonedDateTime?,

    @Lob
    @Column
    val notes: String?,

    val probationAreaId: Long,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff,

    val externalReference: String? = null,

    @Column(name = "description")
    val description: String? = null,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val location: Location? = null,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: AppointmentOutcome? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_generator")
    @Column(name = "contact_id")
    val id: Long = 0
) {
    var partitionAreaId: Long = 0

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    var createdByUserId: Long = 0

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0

    val duration: Duration
        get() =
            if (startTime != null && endTime != null) {
                val sTime = startTime.toLocalTime()
                val eTime = endTime.toLocalTime()
                if (sTime <= eTime) {
                    Duration.between(sTime, eTime)
                } else {
                    val start = LocalDateTime.of(date, sTime)
                    val end = LocalDateTime.of(date.plusDays(1), eTime)
                    Duration.between(start, end)
                }
            } else {
                Duration.ZERO
            }
}

@Immutable
@Entity
@Table(name = "r_contact_type")
class AppointmentType(
    val code: String,
    val description: String,
    @Column(name = "attendance_contact")
    @Convert(converter = YesNoConverter::class)
    val attendanceContact: Boolean,
    @Id
    @Column(name = "contact_type_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class AppointmentOutcome(
    val code: String,
    val description: String,
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "office_location")
class Location(
    @Column(name = "code", columnDefinition = "char(7)")
    val code: String,

    val description: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val townCity: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,

    @Id
    @Column(name = "office_location_id")
    val id: Long
)

interface AppointmentRepository : JpaRepository<Appointment, Long> {
    @Query(
        """
        select a from Appointment a
        join fetch a.type t
        join fetch a.staff s
        left join fetch s.user u
        left join fetch a.location
        left join fetch a.outcome
        where a.person.crn = :crn
        and t.attendanceContact = true
        and a.date >= :start and a.date <= :end
        order by a.date desc, a.startTime desc
    """
    )
    fun findAppointmentsFor(crn: String, start: LocalDate, end: LocalDate, pageable: Pageable): Page<Appointment>

    @Query(
        """
            select count(c.contact_id)
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            where c.offender_id = :personId and ct.attendance_contact = 'Y'
            and to_char(c.contact_date, 'YYYY-MM-DD') = :date
            and to_char(c.contact_start_time, 'HH24:MI') < :endTime 
            and to_char(c.contact_end_time, 'HH24:MI') > :startTime
            and c.soft_deleted = 0 and c.contact_outcome_type_id is null
        """,
        nativeQuery = true
    )
    fun getClashCount(
        personId: Long,
        date: String,
        startTime: String,
        endTime: String
    ): Int
}

fun AppointmentRepository.appointmentClashes(
    personId: Long,
    date: LocalDate,
    startTime: ZonedDateTime,
    endTime: ZonedDateTime,
): Boolean = getClashCount(
    personId,
    date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    startTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault())),
    endTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault()))
) > 0

interface AppointmentTypeRepository : JpaRepository<AppointmentType, Long> {
    fun findByCode(code: String): AppointmentType?
}

fun AppointmentTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("AppointmentType", "code", code)
