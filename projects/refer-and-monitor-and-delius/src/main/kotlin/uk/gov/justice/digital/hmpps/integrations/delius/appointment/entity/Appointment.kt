package uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.Immutable
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.time.ZonedDateTime

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "contact")
class Appointment(

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: AppointmentType,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: AppointmentOutcome,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Lob
    @Column
    val notes: String? = null,

    @Id
    @Column(name = "contact_id")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(nullable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    val partitionAreaId: Long = 0
)

@Immutable
@Entity
@Table(name = "r_contact_type")
class AppointmentType(
    val code: String,
    @Id
    @Column(name = "contact_type_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class AppointmentOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,
    val code: String
)
