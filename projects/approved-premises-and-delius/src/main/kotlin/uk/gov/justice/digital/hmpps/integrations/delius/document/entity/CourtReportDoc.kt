package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "court_report")
class CourtReportDoc(
    @Id
    @Column(name = "court_report_id")
    val id: Long,
    @JoinColumn(name = "court_appearance_id", insertable = false, updatable = false)
    @ManyToOne
    val documentCourtAppearance: DocumentCourtAppearance? = null,
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
    val description: String,
)

@Entity
@Immutable
@Table(name = "court_appearance")
class DocumentCourtAppearance(
    @Id
    @Column(name = "court_appearance_id")
    val id: Long,
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    @ManyToOne
    val event: DocEvent,
)
