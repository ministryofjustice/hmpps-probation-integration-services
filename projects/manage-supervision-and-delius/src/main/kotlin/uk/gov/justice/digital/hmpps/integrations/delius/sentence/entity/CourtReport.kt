package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class CourtReport(
    @Id val courtReportId: Long,

    @ManyToOne
    @JoinColumn(name = "court_report_type_id")
    val courtReportType: CourtReportType?,

    @ManyToOne
    @JoinColumn(name = "court_appearance_id")
    val courtAppearance: CourtAppearance
)