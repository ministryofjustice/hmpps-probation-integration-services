package uk.gov.justice.digital.hmpps.integrations.delius.contact.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "contact")
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long = 0,

    @Column(updatable = false)
    val offenderId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType? = null,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "enforcement", columnDefinition = "number")
    val enforcementContact: Boolean? = null,

    @Column(name = "event_id")
    val eventId: Long? = null,

    @Column(name = "attended")
    @Convert(converter = YesNoConverter::class)
    val attended: Boolean? = null,

    @Column(name = "complied")
    @Convert(converter = YesNoConverter::class)
    val complied: Boolean? = null,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: AttendanceOutcome? = null,

    @Column(columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class AttendanceOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,

    @Column
    val description: String
)

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    val code: String,

    val description: String,

    @Column(name = "national_standards_contact", length = 1)
    @Convert(converter = YesNoConverter::class)
    val nationalStandards: Boolean,

    @Column(name = "attendance_contact")
    @Convert(converter = YesNoConverter::class)
    val attendanceContact: Boolean = false,
)

interface AttendanceRepository : JpaRepository<CaseNote, Long> {

    @Query(
        """
            SELECT contact FROM Contact contact
            LEFT OUTER JOIN AttendanceOutcome cot ON cot = contact.outcome
            WHERE contact.offenderId = :personId
            AND contact.eventId = :eventId
            AND contact.date <= :contactDate
            AND (contact.enforcementContact = true OR contact.outcome is not null)
            AND contact.type.attendanceContact = true
            AND contact.type.nationalStandards = true
        """
    )
    fun findByOffenderAndEventId(eventId: Long, personId: Long, contactDate: LocalDate): List<Contact>
}

