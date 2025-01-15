package uk.gov.justice.digital.hmpps.integrations.delius.nsi.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.nsi.EnforcementActivityCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "nsi")
@SQLRestriction("soft_deleted = 0")
class Nsi(

    @Id
    @Column(name = "nsi_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: NsiEvent?,

    val referralDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "nsi_outcome_id")
    val outcome: ReferenceData?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

@Immutable
@Entity
@Table(name = "event")
@SQLRestriction("soft_deleted = 0")
class NsiEvent(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

interface NsiRepository : JpaRepository<Nsi, Long> {
    @Query(
        """
        select count(nsi) 
        from Nsi nsi
        join nsi.event e
        where nsi.personId = :personId
        and nsi.referralDate >= :referralDate
        and nsi.outcome.code in :outcomes
    """
    )
    fun countByPersonIdAndOutcomeIn(personId: Long, referralDate: LocalDate, outcomes: List<String>): Int
}

fun NsiRepository.previousEnforcementActivity(personId: Long): Boolean =
    countByPersonIdAndOutcomeIn(personId, LocalDate.now().minusDays(365), EnforcementActivityCode.stringValues) > 0
