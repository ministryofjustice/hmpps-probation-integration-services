package uk.gov.justice.digital.hmpps.integration.delius.registration.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integration.delius.person.Person
import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integration.delius.reference.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "registration")
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
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
    @OrderBy("date, createdDateTime")
    val reviews: List<RegistrationReview>,

    @Column(name = "deregistered", columnDefinition = "number")
    val deRegistered: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @Column(name = "created_datetime")
    val createdDateTime: ZonedDateTime,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

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

    @Column(name = "register_review_period")
    val reviewPeriod: Long?,

    val colour: String?,

    @Id
    @Column(name = "register_type_id")
    val id: Long
)

@Entity
@Table(name = "registration_review")
@SQLRestriction("soft_deleted = 0")
class RegistrationReview(

    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: Registration,

    @Column(name = "review_date")
    val date: LocalDate,

    @Column(name = "review_date_due")
    val reviewDue: LocalDate?,

    @Column(columnDefinition = "clob")
    val notes: String?,

    @ManyToOne
    @JoinColumn(name = "reviewing_team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "reviewing_staff_id")
    val staff: Staff,

    @Convert(converter = YesNoConverter::class)
    val completed: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Column(name = "created_datetime")
    val createdDateTime: ZonedDateTime,

    @Id
    @Column(name = "registration_review_id")
    val id: Long
)

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @EntityGraph(attributePaths = ["type.flag", "category", "level", "team", "staff", "reviews.team", "reviews.staff"])
    fun findAllByPersonCrnOrderByDateDescCreatedDateTimeDesc(crn: String): List<Registration>
}