package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "contact")
@SQLRestriction("soft_deleted = 0")
class Contact(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event?,

    @ManyToOne
    @JoinColumn(name = "rqmnt_id")
    val requirement: Requirement?,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val location: OfficeLocation?,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: ContactOutcome?,

    val description: String?,

    @Lob
    @Column
    val notes: String?,

    @Column(nullable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "contact_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    override val code: String,
    override val description: String,
    @Id
    @Column(name = "contact_type_id")
    val id: Long
) : CodeAndDescription

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    override val code: String,
    override val description: String,

    @Convert(converter = YesNoConverter::class)
    val enforceable: Boolean?,

    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long
) : CodeAndDescription

interface ContactRepository : JpaRepository<Contact, Long> {
    fun findByPersonCrnAndOutcomeEnforceableTrue(crn: String): List<Contact>
}