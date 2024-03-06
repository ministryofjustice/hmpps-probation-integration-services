package uk.gov.justice.digital.hmpps.integrations.delius.registration.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
@Table(name = "registration")
class Registration(
    @Id
    @Column(name = "registration_id", nullable = false)
    val id: Long,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @Column(name = "registration_date")
    val startDate: LocalDate,

    @Column(name = "registration_notes", columnDefinition = "clob")
    val notes: String?,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val registerType: RegisterType,

    @Fetch(FetchMode.JOIN)
    @OneToMany(mappedBy = "registration")
    val deRegistrations: List<DeRegistration>,

    @Column(name = "deregistered", columnDefinition = "number")
    val deRegistered: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,
) {
    val endDate: LocalDate?
        get() = if (deRegistrations.isEmpty()) null else deRegistrations.maxOf { it.deRegistrationDate }
}

@Entity
@Immutable
@Table(name = "r_register_type")
class RegisterType(
    @Id
    @Column(name = "register_type_id")
    val id: Long,
    val description: String,

    @ManyToOne
    @JoinColumn(name = "register_type_flag_id")
    val flag: ReferenceData?,

    val colour: String?,
)

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
@Table(name = "deregistration")
class DeRegistration(
    @Id
    @Column(name = "deregistration_id", nullable = false)
    val id: Long,

    @Column(name = "deregistration_date")
    val deRegistrationDate: LocalDate,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "registration_id", nullable = false)
    val registration: Registration,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean
)
