package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "contact")
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
    val startTime: ZonedDateTime,

    @Column(name = "contact_end_time")
    val endTime: ZonedDateTime?,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val location: Location?,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff,

    @Column(name = "description")
    val description: String?,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: AppointmentOutcome?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "contact_id")
    val id: Long
) {
    val duration: Duration
        get() =
            if (endTime != null) {
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
}
