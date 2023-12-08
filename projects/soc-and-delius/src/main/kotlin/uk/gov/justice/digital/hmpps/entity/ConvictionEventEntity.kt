package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
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
    val softDeleted: Boolean = false,
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
    val softDeleted: Boolean = false,
)

@Immutable
@Table(name = "disposal")
@Entity
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
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
    val expectedEndDate: LocalDate? = null,
    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,
    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,
)

@Immutable
@Table(name = "r_disposal_type")
@Entity
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,
    @Column
    val description: String,
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
    val softDeleted: Boolean = false,
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
    val softDeleted: Boolean = false,
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
)

interface ConvictionEventRepository : JpaRepository<ConvictionEventEntity, Long> {
    @EntityGraph(
        attributePaths = [
            "mainOffence.offence",
            "additionalOffences.offence",
            "disposal.type",
        ],
    )
    fun getAllByConvictionEventPersonIdOrderByConvictionDateDesc(personId: Long): List<ConvictionEventEntity>

    @EntityGraph(
        attributePaths = [
            "mainOffence.offence",
            "additionalOffences.offence",
            "disposal.type",
        ],
    )
    fun getAllByConvictionEventPersonCrn(crn: String): List<ConvictionEventEntity>

    @EntityGraph(
        attributePaths = [
            "mainOffence.offence",
            "additionalOffences.offence",
            "disposal.type",
        ],
    )
    fun getAllByConvictionEventPersonNomsNumber(nomsNumber: String): List<ConvictionEventEntity>

    @EntityGraph(
        attributePaths = [
            "mainOffence.offence",
            "additionalOffences.offence",
            "disposal.type",
        ],
    )
    fun getAllByConvictionEventPersonCrnAndActiveIsTrue(crn: String): List<ConvictionEventEntity>

    @EntityGraph(
        attributePaths = [
            "mainOffence.offence",
            "additionalOffences.offence",
            "disposal.type",
        ],
    )
    fun getAllByConvictionEventPersonNomsNumberAndActiveIsTrue(nomsNumber: String): List<ConvictionEventEntity>
}

fun ConvictionEventRepository.getLatestConviction(personId: Long) =
    getAllByConvictionEventPersonIdOrderByConvictionDateDesc(personId).firstOrNull()
