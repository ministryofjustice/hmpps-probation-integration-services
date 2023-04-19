package uk.gov.justice.digital.hmpps.epf.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "court_appearance")
@Where(clause = "soft_deleted = 0")
class CourtAppearance(

    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @Column(name = "appearance_date")
    val appearanceDate: LocalDate,

    @JoinColumn(name = "event_id")
    @ManyToOne
    val event: Event,

    @JoinColumn(name = "court_id")
    @ManyToOne
    val court: Court,

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
        select ca.court.name as name from CourtAppearance ca
        where ca.event.id = :eventId
        order by ca.appearanceDate desc
    """
    )
    fun findLatestCourtNameByEventId(eventId: Long, page: PageRequest = PageRequest.of(0, 1)): String
}
