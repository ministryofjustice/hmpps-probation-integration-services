package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Immutable
class Court(

    @Id
    @Column(name = "court_id")
    val id: Long,

    @Column(columnDefinition = "char(6)")
    val code: String,

    @Column(name = "court_name")
    val name: String

)

@Entity
@Immutable
@Table(name = "court_appearance")
@SQLRestriction("soft_deleted = 0")
class CourtAppearanceEntity(

    @Column(name = "appearance_date")
    val appearanceDate: ZonedDateTime,

    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @JoinColumn(name = "event_id")
    @ManyToOne
    val courtAppearanceEventEntity: CourtAppearanceEventEntity,

    @ManyToOne
    @JoinColumn(name = "appearance_type_id")
    val appearanceType: ReferenceData,

    @JoinColumn(name = "court_id")
    @ManyToOne
    val court: Court,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "event")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class CourtAppearanceEventEntity(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val courtAppearancePerson: CourtAppearancePerson,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "offender")
@Entity
@SQLRestriction("soft_deleted = 0")
class CourtAppearancePerson(
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

interface CourtAppearanceRepository : JpaRepository<CourtAppearanceEntity, Long> {

    @Query(
        """
        select ca from CourtAppearanceEntity ca
        where ca.appearanceDate >= :dateFrom
        and ca.courtAppearanceEventEntity.courtAppearancePerson.crn = :crn
        order by ca.appearanceDate desc
        """
    )
    fun findMostRecentCourtAppearancesByCrn(dateFrom: ZonedDateTime, crn: String): List<CourtAppearanceEntity>

    @Query(
        """
        select ca from CourtAppearanceEntity ca
        where ca.appearanceDate >= :dateFrom
        and ca.courtAppearanceEventEntity.courtAppearancePerson.nomsNumber = :nomsNumber
        order by ca.appearanceDate desc
        """
    )
    fun findMostRecentCourtAppearancesByNomsNumber(
        dateFrom: ZonedDateTime,
        nomsNumber: String
    ): List<CourtAppearanceEntity>

    @Query(
        """
        select ca from CourtAppearanceEntity ca
        where ca.appearanceDate >= :dateFrom
        and ca.courtAppearanceEventEntity.courtAppearancePerson.crn in :crns
        order by ca.courtAppearanceEventEntity.courtAppearancePerson.crn, ca.appearanceDate desc
        """
    )
    fun findCourtAppearancesForCrns(
        crns: List<String>,
        dateFrom: ZonedDateTime = LocalDate.now().atStartOfDay(EuropeLondon)
    ): List<CourtAppearanceEntity>
}
