package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*

@Entity
@Table(name = "court_report")
class CourtReport(
    @Id
    @Column(name = "court_report_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "court_appearance_id")
    val courtAppearance: CourtAppearance,

)