package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "court_appearance")
@SQLRestriction("soft_deleted = 0")
class CourtAppearance(
    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    val eventId: Long,

    @ManyToOne
    @JoinColumn(name = "court_id")
    val court: CourtEntity,

    @ManyToOne
    @JoinColumn(name = "appearance_type_id")
    val appearanceType: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "outcome_id")
    val outcome: ReferenceData,

    val softDeleted: Int = 0

)

@Entity
@Table(name = "court")
class CourtEntity(
    @Id
    @Column(name = "court_id")
    val id: Long,

    val courtName: String
)

interface CourtAppearanceRepository : JpaRepository<CourtAppearance, Long> {
    fun findByEventIdAndAppearanceTypeCode(eventId: Long, appearanceTypeCode: String): List<CourtAppearance>
}