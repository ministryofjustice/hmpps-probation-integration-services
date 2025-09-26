package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.exception.NotFoundException
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
    val startTime: ZonedDateTime?,

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

    @CreatedDate
    @Column("createdDatetime")
    val createdDateTime: ZonedDateTime,

    @LastModifiedDate
    @Column("lastUpdatedDatetime")
    val lastUpdatedDateTime: ZonedDateTime,

    @Id
    @Column(name = "contact_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    val code: String,
    val description: String,

    @Id
    @Column(name = "contact_type_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    val code: String,
    val description: String,

    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long
)

interface ContactRepository : JpaRepository<Contact, Long>

fun ContactRepository.getContact(id: Long): Contact = findByIdOrNull(id) ?: throw NotFoundException("Contact", "id", id)