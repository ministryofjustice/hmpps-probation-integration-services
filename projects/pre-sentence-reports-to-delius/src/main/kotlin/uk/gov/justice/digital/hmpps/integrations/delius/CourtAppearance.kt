package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*

@Entity
@Table(name = "court_appearance")
class CourtAppearance(
    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,
) {
}