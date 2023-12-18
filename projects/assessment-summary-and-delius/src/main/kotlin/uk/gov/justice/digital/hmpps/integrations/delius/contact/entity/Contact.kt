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
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.AuditableEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.time.LocalDate

@Entity
@Table(name = "contact")
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
class Contact(

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    val eventId: Long? = null,

    @Column(name = "alert_active")
    @Convert(converter = YesNoConverter::class)
    val alert: Boolean? = false,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "contact_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_seq")
    val id: Long = 0
) : AuditableEntity() {

    @Column(name = "contact_date")
    var date: LocalDate = LocalDate.now()
        private set
    var teamId: Long = 0
        private set
    var staffId: Long = 0
        private set

    fun withDateTeamAndStaff(date: LocalDate, teamId: Long, staffId: Long): Contact {
        this.date = date
        this.teamId = teamId
        this.staffId = staffId
        return this
    }

    @Lob
    @Column
    var notes: String? = null
        private set

    fun withNotes(notes: String?): Contact {
        this.notes = (this.notes ?: "") + """${System.lineSeparator()}
            |$notes
        """.trimMargin()
        return this
    }
}

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    val code: String,
    @Id
    @Column(name = "contact_type_id")
    val id: Long
) {
    enum class Code(val value: String) {
        DEREGISTRATION("ERGD"),
        REGISTRATION("ERGN"),
        REGISTRATION_REVIEW("ERGR"),
        OASYS_ASSESSMENT("EOAS"),
        OASYS_ASSESSMENT_COMPLETED("C004"),
        SENTENCE_PLAN_INITIAL("CSPI"),
        SENTENCE_PLAN_REVIEW("CSPR")
    }
}

interface ContactRepository : JpaRepository<Contact, Long>
interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Contact", "code", code)
