package uk.gov.justice.digital.hmpps.integration.delius.registration.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.LocalDate
import java.time.LocalDateTime

@Immutable
@Entity
@Table(name = "registration")
@SQLRestriction("soft_deleted = 0")
class Registration(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    val level: ReferenceData?,

    @Column(name = "registration_date")
    val date: LocalDate,

    @Column(name = "next_review_date")
    val reviewDate: LocalDate?,

    @Column(name = "registration_notes", columnDefinition = "clob")
    val notes: String?,

    @ManyToOne
    @JoinColumn(name = "registering_team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "registering_staff_id")
    val staff: Staff,

    @OneToMany(mappedBy = "registration")
    val deRegistrations: List<DeRegistration> = emptyList(),

    @Column(name = "deregistered", columnDefinition = "number")
    val deRegistered: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @Column(name = "created_datetime")
    val createdDateTime: LocalDateTime,

    @Id
    @Column(name = "registration_id")
    val id: Long
) {
    fun latestDeregistration(): DeRegistration? =
        deRegistrations.sortedWith(
            compareByDescending(DeRegistration::deRegistrationDate)
                .thenByDescending(DeRegistration::createdDateTime)
        ).firstOrNull()
}

@Immutable
@Table(name = "r_register_type")
@Entity
class RegisterType(
    @Column
    val code: String,

    @Column
    val description: String,

    @ManyToOne
    @JoinColumn(name = "register_type_flag_id")
    val flag: ReferenceData?,

    @Convert(converter = YesNoConverter::class)
    val alertMessage: Boolean,

    @Column(name = "register_review_period")
    val reviewPeriod: Long?,

    val colour: String?,

    @Id
    @Column(name = "register_type_id")
    val id: Long
)

@Immutable
@Entity
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

    @ManyToOne
    @JoinColumn(name = "deregistering_team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "deregistering_staff_id")
    val staff: Staff,

    @Column(name = "deregistering_notes", columnDefinition = "clob")
    val notes: String?,

    @Column(name = "created_datetime")
    val createdDateTime: LocalDateTime,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean
)

interface RegistrationRepository : JpaRepository<Registration, Long> {

    fun findByPersonId(personId: Long): List<Registration>

    @Query(
        "select registration from Registration registration " +
            "where registration.person.id = :personId " +
            "and registration.softDeleted = false " +
            "and registration.deRegistered = false "
    )
    fun findActiveByPersonId(personId: Long): List<Registration>
}