package uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event

@Entity
@Immutable
@Table(name = "court_appearance")
@Where(clause = "soft_deleted = 0")
class CourtAppearance(
    @JoinColumn(name = "event_id")
    @ManyToOne
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "outcome_id")
    val outcome: Outcome,

    @Column(name = "court_id")
    val courtId: Long,

    @Column(name = "soft_deleted", columnDefinition = "number")
    var softDeleted: Boolean,

    @Id
    @Column(name = "court_appearance_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class Outcome(

    @Column(name = "code_value")
    val code: String,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
) {
    enum class Code(val value: String) {
        AWAITING_PSR("101")
    }
}
