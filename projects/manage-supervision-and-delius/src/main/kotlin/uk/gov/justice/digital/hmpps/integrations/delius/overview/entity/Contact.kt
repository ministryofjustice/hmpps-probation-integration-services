package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "contact")
@SQLRestriction("soft_deleted = 0")
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long = 0,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "contact_date")
    val date: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "rar_activity", length = 1)
    @Convert(converter = YesNoConverter::class)
    var rarActivity: Boolean? = null,

    @Column(name = "attended")
    @Convert(converter = YesNoConverter::class)
    var attended: Boolean? = null,

    @Column(name = "complied")
    @Convert(converter = YesNoConverter::class)
    var complied: Boolean? = null,

    @Column(name = "rqmnt_id")
    val requirementId: Long? = null,

    @Column(name = "contact_end_time")
    val endTime: ZonedDateTime? = null,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    var softDeleted: Boolean = false
) {
    fun startDateTime(): ZonedDateTime = ZonedDateTime.of(date.toLocalDate(), startTime.toLocalTime(), date.zone)
}

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
)

interface ContactRepository : JpaRepository<Contact, Long> {

    @Query(
        """
            select c from Contact c
            join fetch c.type t
            where c.personId = :personId
            and c.type.attendanceContact = true
            and (c.date > CURRENT_TIMESTAMP OR (c.date = CURRENT_DATE AND c.startTime > :timeNow))
            order by c.date, c.startTime asc
        """
    )
    fun findFirstAppointment(
        personId: Long,
        timeNow: ZonedDateTime = ZonedDateTime.of(LocalDate.EPOCH, LocalTime.now(), ZoneId.systemDefault()),
        pageable: Pageable = PageRequest.of(0, 1)
    ): List<Contact>
}