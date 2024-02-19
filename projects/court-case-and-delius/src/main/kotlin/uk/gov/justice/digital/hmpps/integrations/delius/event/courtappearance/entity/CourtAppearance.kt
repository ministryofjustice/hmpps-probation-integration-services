package uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "court_appearance")
@SQLRestriction("soft_deleted = 0")
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

interface CourtReportRepository : JpaRepository<CourtReport, Long> {

    @Query(
        """
       select c from CourtReport c
       where c.courtAppearance.event = :event
    """
    )
    fun getAllByEvent(event: Event): List<CourtReport>
}

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

@Entity
@Table(name = "court_report")
@SQLRestriction("soft_deleted = 0")
class CourtReport(
    @Column(name = "date_requested")
    val dateRequested: LocalDate,
    @Column(name = "date_required")
    val dateRequired: LocalDate,
    @Column(name = "completed_Date")
    val dateCompleted: LocalDate?,

    @ManyToOne
    @JoinColumn(name = "court_report_type_id")
    val courtReportType: CourtReportType?,

    @ManyToOne
    @JoinColumn(name = "delivered_court_report_type_id")
    val deliveredCourtReportType: CourtReportType?,

    @ManyToOne
    @JoinColumn(name = "court_appearance_id")
    val courtAppearance: CourtAppearance,

    @OneToMany(mappedBy = "courtReport")
    val reportManagers: List<ReportManager> = listOf(),

    @Column(name = "soft_deleted", columnDefinition = "number")
    var softDeleted: Boolean = false,

    @Id
    @Column(name = "court_report_id")
    val id: Long
)

@Entity
@Table(name = "report_manager")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class ReportManager(

    @JoinColumn(name = "court_report_id")
    @ManyToOne
    val courtReport: CourtReport,

    @JoinColumn(name = "staff_id")
    @OneToOne
    val staff: Staff,

    @Column(name = "active_flag", columnDefinition = "number")
    var active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    var softDeleted: Boolean,

    @Id
    @Column(name = "report_manager_id")
    val id: Long
)

@Entity
@Table(name = "r_court_report_type")
class CourtReportType(
    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String,

    @Id
    @Column(name = "court_report_type_id")
    val id: Long
)
