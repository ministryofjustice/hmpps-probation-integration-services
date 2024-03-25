package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class CourtAppearance(
    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @Column(name = "appearance_date")
    val date: LocalDate,

    @JoinColumn(name = "court_id")
    @ManyToOne
    val court: Court,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    var softDeleted: Boolean = false
)
