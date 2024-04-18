package uk.gov.justice.digital.hmpps.epf.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Immutable
class Court(

    @Id
    @Column(name = "court_id")
    val id: Long,

    @Column(name = "court_name")
    val name: String

)

@Entity
@Immutable
@Table(name = "court_appearance")
@SQLRestriction("soft_deleted = 0")
class CourtAppearance(

    @Column(name = "appearance_date")
    val appearanceDate: LocalDate,

    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @JoinColumn(name = "event_id")
    @ManyToOne
    val event: Event,

    @JoinColumn(name = "court_id")
    @ManyToOne
    val court: Court,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    val softDeleted: Boolean = false
)

interface CourtAppearanceRepository : JpaRepository<CourtAppearance, Long> {

    @EntityGraph(attributePaths = ["court"])
    fun findByEventIdOrderByAppearanceDateDesc(
        eventId: Long,
        page: PageRequest = PageRequest.of(0, 1)
    ): CourtAppearance?
}
