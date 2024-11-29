package uk.gov.justice.digital.hmpps.service.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Contact(
    @Id
    @Column(name = "contact_id", updatable = false)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "contact_type_id", updatable = false)
    val type: ContactType,

    @Column(name = "contact_date")
    val date: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "rar_activity", length = 1)
    @Convert(converter = YesNoConverter::class)
    val rarActivity: Boolean? = null,

    @Column(name = "attended")
    @Convert(converter = YesNoConverter::class)
    val attended: Boolean? = null,

    @Column(name = "sensitive")
    @Convert(converter = YesNoConverter::class)
    val sensitive: Boolean? = null,

    @Column(name = "complied")
    @Convert(converter = YesNoConverter::class)
    val complied: Boolean? = null,

    @ManyToOne
    @JoinColumn(name = "rqmnt_id")
    val requirement: Requirement? = null,

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    var softDeleted: Boolean = false

)

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,
    val code: String
)

interface ContactRepository : JpaRepository<Contact, Long> {

    @Query(
        """
        select max(to_date(to_char(c.contact_date, 'yyyy-mm-dd') || ' ' || to_char(c.contact_start_time, 'hh24:mi:ss'), 'yyyy-mm-dd hh24:mi:ss'))
        from contact c
        join event e on c.event_id = e.event_id
        join offender o on e.offender_id = o.offender_id
        join r_contact_type ct on c.contact_type_id = ct.contact_type_id
        where o.crn = :crn
        and e.ACTIVE_FLAG = 1
        and e.SOFT_DELETED = 0
        and c.SOFT_DELETED = 0
        and ct.code in ('COAI', 'COVI', 'CODI', 'COHV')
        """,
        nativeQuery = true
    )
    fun getFirstAppointmentDate(crn: String): LocalDateTime?
}
