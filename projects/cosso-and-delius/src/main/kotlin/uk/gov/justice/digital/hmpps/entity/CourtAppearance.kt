package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Entity
@SQLRestriction("soft_deleted = 0")
class CourtAppearance(
    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    val eventId: Long,

    val appearanceDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "court_id")
    val court: CourtEntity,

    @ManyToOne
    @JoinColumn(name = "appearance_type_id")
    val appearanceType: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "outcome_id")
    val outcome: ReferenceData,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean

)

@Entity
@Table(name = "court")
class CourtEntity(
    @Id
    @Column(name = "court_id")
    val id: Long,
    val courtName: String,
)

interface CourtAppearanceRepository : JpaRepository<CourtAppearance, Long> {

    @Query(
        """
        select ca from CourtAppearance ca
        where ca.eventId = :eventId
        and ca.appearanceType.code = 'S'
        and ca.outcome.id is not null
        order by ca.appearanceDate
        """
    )
    fun findSentencingAppearance(eventId: Long): List<CourtAppearance>
}