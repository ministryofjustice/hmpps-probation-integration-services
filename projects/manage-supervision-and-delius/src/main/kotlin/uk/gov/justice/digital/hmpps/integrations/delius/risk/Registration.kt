package uk.gov.justice.digital.hmpps.integrations.delius.risk

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
@Table(name = "registration")
class RiskFlag(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(name = "deregistered", columnDefinition = "number")
    val deRegistered: Boolean,

    @Column(name = "registration_notes", columnDefinition = "clob")
    val notes: String?,

    @Column(name = "created_datetime")
    val createdDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    val createdBy: User,

    @Column(name = "next_review_date")
    val nextReviewDate: LocalDate,

    @OneToMany(mappedBy = "registration")
    val reviews: List<RegistrationReview> = emptyList(),

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

interface RiskFlagRepository : JpaRepository<RiskFlag, Long> {
    fun findByPersonId(personId: Long): List<RiskFlag>
    fun findByPersonIdAndId(personId: Long, id: Long): RiskFlag?
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
    val completed: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Column(name = "created_datetime")
    val createdDateTime: ZonedDateTime,

    @Id
    @Column(name = "registration_review_id")
    val id: Long
)
