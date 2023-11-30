package uk.gov.justice.digital.hmpps.service.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
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

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "contact_type_id", updatable = false)
    val type: ContactType,

    @Column(name = "contact_date")
    val date: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
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
