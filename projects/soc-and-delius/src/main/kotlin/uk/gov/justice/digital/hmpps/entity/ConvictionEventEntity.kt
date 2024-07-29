package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Immutable
@Table(name = "event")
@Entity
@SQLRestriction("soft_deleted = 0")
class ConvictionEventEntity(
    @Id
    @Column(name = "event_id")
    val id: Long,

    val convictionDate: LocalDate?,

    val referralDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val convictionEventPerson: ConvictionEventPerson,

    @OneToOne(mappedBy = "event")
    val mainOffence: MainOffence? = null,

    @OneToMany(mappedBy = "event")
    val additionalOffences: List<AdditionalOffence> = listOf(),

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "offender")
@Entity
@SQLRestriction("soft_deleted = 0")
class ConvictionEventPerson(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(columnDefinition = "char(7)")
    val nomsNumber: String? = null,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false

)

@Immutable
@Table(name = "disposal")
@Entity
@SQLRestriction("soft_deleted = 0")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: ConvictionEventEntity,

    @Column(name = "disposal_date")
    val startDate: LocalDate,

    @Column(name = "notional_end_date")
    val notionalEndDate: LocalDate? = null,

    @Column(name = "entered_notional_end_date")
    val enteredEndDate: LocalDate? = null,

    @OneToOne(mappedBy = "disposal")
    val custody: Custody? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
) {
    fun expectedEndDate() = enteredEndDate ?: notionalEndDate
}

@Immutable
@Table(name = "r_disposal_type")
@Entity
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column
    val description: String
)

@Immutable
@Table(name = "main_offence")
@Entity
@SQLRestriction("soft_deleted = 0")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: ConvictionEventEntity?,

    @JoinColumn(name = "offence_id")
    @ManyToOne
    val offence: Offence,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "additional_offence")
@Entity
@SQLRestriction("soft_deleted = 0")
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: ConvictionEventEntity?,

    @JoinColumn(name = "offence_id")
    @ManyToOne()
    val offence: Offence,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "r_offence")
@Entity
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column(columnDefinition = "char(5)")
    val code: String,

    @Column
    val description: String,

    @Column
    val mainCategoryDescription: String,
)

@Entity
@Immutable
@Table(name = "court_appearance")
@SQLRestriction("soft_deleted = 0")
class ConvictionCourtAppearanceEntity(
    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @JoinColumn(name = "event_id")
    @ManyToOne
    val convictionEventEntity: ConvictionEventEntity,

    @ManyToOne
    @JoinColumn(name = "outcome_id")
    val outcome: ReferenceData,

    @Column(name = "appearance_date")
    val appearanceDate: LocalDate,

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false
)

interface ConvictionEventRepository : JpaRepository<ConvictionEventEntity, Long> {
    @Query(
        """
        select c from ConvictionEventEntity c
        join fetch c.disposal d 
        join fetch d.type dt
        join fetch c.mainOffence mo
        join fetch mo.offence
        join fetch d.custody cust
        left join fetch c.additionalOffences ao
        left join fetch ao.offence
        where c.convictionEventPerson.id = :personId and c.active = true
        and cust.status.code <> 'P'
        order by c.convictionDate desc
    """
    )
    fun getActiveSentencedConvictions(personId: Long): List<ConvictionEventEntity>

    @EntityGraph(
        attributePaths = [
            "mainOffence.offence",
            "additionalOffences.offence",
            "disposal.type"
        ]
    )
    fun getAllByConvictionEventPersonCrn(crn: String): List<ConvictionEventEntity>

    @EntityGraph(
        attributePaths = [
            "mainOffence.offence",
            "additionalOffences.offence",
            "disposal.type"
        ]
    )
    fun getAllByConvictionEventPersonNomsNumber(nomsNumber: String): List<ConvictionEventEntity>

    @EntityGraph(
        attributePaths = [
            "mainOffence.offence",
            "additionalOffences.offence",
            "disposal.type"
        ]
    )
    fun getAllByConvictionEventPersonCrnAndActiveIsTrue(crn: String): List<ConvictionEventEntity>

    @EntityGraph(
        attributePaths = [
            "mainOffence.offence",
            "additionalOffences.offence",
            "disposal.type"
        ]
    )
    fun getAllByConvictionEventPersonNomsNumberAndActiveIsTrue(nomsNumber: String): List<ConvictionEventEntity>

    @Query(
        """
        select ca.outcome.description from ConvictionCourtAppearanceEntity ca
        where ca.convictionEventEntity.id = :eventId
        order by ca.appearanceDate desc
        """
    )
    fun findLatestCourtAppearanceOutcome(eventId: Long, pageRequest: PageRequest = PageRequest.of(0, 1)): String?
}

fun ConvictionEventRepository.getLatestConviction(personId: Long) =
    getActiveSentencedConvictions(personId).firstOrNull()
