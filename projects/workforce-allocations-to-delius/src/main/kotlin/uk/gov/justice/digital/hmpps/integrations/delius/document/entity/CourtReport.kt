package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Immutable
class CourtReport(
    @Id
    @Column(name = "court_report_id")
    val id: Long,

    @JoinColumn(name = "court_appearance_id", insertable = false, updatable = false)
    @ManyToOne
    val courtAppearance: CourtAppearance? = null,

    @ManyToOne
    @JoinColumn(name = "court_report_type_id", updatable = false)
    val type: CourtReportType,
)

@Immutable
@Entity
@Table(name = "r_court_report_type")
class CourtReportType(
    @Id
    @Column(name = "court_report_type_id")
    val id: Long,

    val description: String
)

@Entity
@Immutable
class CourtAppearance(

    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ManyToOne
    val event: DocEvent
)
