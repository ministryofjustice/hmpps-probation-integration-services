package uk.gov.justice.digital.hmpps.integrations.delius.courtappearance

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "court_appearance")
@SQLRestriction("soft_deleted = 0")
class CourtAppearance(

    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @Column(name = "appearance_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "appearance_type_id")
    val type: ReferenceData,

    @JoinColumn(name = "event_id")
    @ManyToOne
    val event: Event,

    @JoinColumn(name = "court_id")
    @ManyToOne
    val court: Court,

    val outcomeId: Long,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    var softDeleted: Boolean = false
)

@Entity
@Immutable
class Court(

    @Id
    @Column(name = "court_id")
    val id: Long,

    @Column(name = "court_name")
    val name: String

)

interface CourtRepository : JpaRepository<Court, Long>
interface CourtAppearanceRepository : JpaRepository<CourtAppearance, Long> {

    @Query(
        """
        select ca.court.name as name, ca.date as appearanceDate from CourtAppearance ca
        where ca.event.id = :eventId
        and ca.type.code = 'S'
        and ca.outcomeId is not null
        order by ca.date
    """
    )
    fun findOriginalCourt(
        eventId: Long,
        page: PageRequest = PageRequest.of(0, 1)
    ): uk.gov.justice.digital.hmpps.api.model.Court?
}
