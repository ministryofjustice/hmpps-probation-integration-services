package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import java.time.ZonedDateTime

@Entity
@Immutable
@Where(clause = "soft_deleted = 0")
class CourtAppearance(
    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @Column(name = "appearance_date")
    val date: ZonedDateTime,

    @Column
    val courtId: Long,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event? = null,

    @ManyToOne
    @JoinColumn(name = "outcome_id")
    val outcome: ReferenceData?,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)
