package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
    val date: LocalDate = LocalDate.now(),

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
    fun startDateTime(): ZonedDateTime = ZonedDateTime.of(date, startTime.toLocalTime(), EuropeLondon)
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
            select c.*
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            where c.offender_id = :personId and ct.attendance_contact = 'Y'
            and (to_char(c.contact_date, 'YYYY-MM-DD') > :dateNow
            or (to_char(c.contact_date, 'YYYY-MM-DD') = :dateNow and to_char(c.contact_start_time, 'HH24:MI') > :timeNow))
            and c.soft_deleted = 0
            order by c.contact_date, c.contact_start_time asc
        """,
        nativeQuery = true
    )
    fun findFirstAppointment(
        personId: Long,
        dateNow: String,
        timeNow: String,
        pageable: Pageable = PageRequest.of(0, 1)
    ): List<Contact>
}

fun ContactRepository.firstAppointment(
    personId: Long,
    date: LocalDate = LocalDate.now(),
    startTime: ZonedDateTime = ZonedDateTime.now()
): Contact? = findFirstAppointment(
    personId,
    date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    startTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(EuropeLondon))
).firstOrNull()