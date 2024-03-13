package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class CourtAppearance(
    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @Column(name = "appearance_date")
    val date: LocalDate,

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
