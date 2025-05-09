package uk.gov.justice.digital.hmpps.integrations.delius.risk

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
@Table(name = "registration")
class RiskFlag(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData?,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deRegistered: Boolean,

    @Column(name = "registration_notes", columnDefinition = "clob")
    val notes: String?,

    @Column(name = "created_datetime")
    val createdDate: LocalDate,

    @OneToMany
    @JoinColumn(name = "registration_id")
    val deRegistrations: List<DeRegistration>,

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    val createdBy: User,

    @Column(name = "next_review_date")
    val nextReviewDate: LocalDate,

    @OneToMany(mappedBy = "registration")
    val reviews: List<RegistrationReview> = emptyList(),

    @Column(name = "registration_date")
    val date: LocalDate,

    @Column(name = "last_updated_datetime")
    val lastUpdated: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    val level: ReferenceData?,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

interface RiskFlagRepository : JpaRepository<RiskFlag, Long> {
    fun findByPersonId(personId: Long): List<RiskFlag>
    fun findByPersonIdAndId(personId: Long, id: Long): RiskFlag?

    @Query(
        """
        select r from RiskFlag r
        left join fetch r.category c
        left join fetch r.type t
        left join fetch r.level l
        where r.type.code = 'MAPP'
        and r.personId = :offenderId
        and r.softDeleted = false
        and r.deRegistered = false
        order by r.date desc
    """
    )
    fun findActiveMappaRegistrationByOffenderId(offenderId: Long, pageable: Pageable): Page<RiskFlag>
}

fun RiskFlagRepository.getRiskFlag(personId: Long, id: Long): RiskFlag =
    findByPersonIdAndId(personId, id) ?: throw NotFoundException("Risk", "id", id)

@Entity
@Table(name = "registration_review")
@SQLRestriction("soft_deleted = 0")
class RegistrationReview(

    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: RiskFlag,

    @Column(name = "review_date")
    val date: LocalDate,

    @Column(name = "review_date_due")
    val reviewDue: LocalDate?,

    @Column(columnDefinition = "clob")
    val notes: String?,

    @Convert(converter = YesNoConverter::class)
    val completed: Boolean?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Column(name = "created_datetime")
    val createdDateTime: ZonedDateTime,

    @Id
    @Column(name = "registration_review_id")
    val id: Long
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

    @ManyToOne
    @JoinColumn(name = "registration_id", nullable = false)
    val registration: RiskFlag,

    @Column(name = "deregistering_notes", columnDefinition = "clob")
    val notes: String?,

    @ManyToOne
    @JoinColumn(name = "deregistering_staff_id")
    val staff: Staff,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

