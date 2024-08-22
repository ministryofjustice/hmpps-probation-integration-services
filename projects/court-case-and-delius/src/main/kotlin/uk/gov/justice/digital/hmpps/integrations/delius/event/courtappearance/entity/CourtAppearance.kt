package uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import java.time.LocalDateTime

@Entity
@Immutable
@Table(name = "court_appearance")
class CourtAppearance(

    @JoinColumn(name = "event_id")
    @ManyToOne(fetch = FetchType.LAZY)
    val event: Event,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outcome_id")
    val outcome: Outcome?,

    val appearanceDate: LocalDateTime,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appearance_type_id")
    val appearanceType: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "court_id")
    val court: Court,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Id
    @Column(name = "court_appearance_id")
    val id: Long,

    @Column(name = "crown_court_calendar_number")
    val crownCourtCalendarNumber: String? = null,

    @Column(name = "bail_conditions")
    val bailConditions: String? = null,

    @Column(name = "court_notes")
    val courtNotes: String? = null,

    @Column(name = "team_id")
    val teamId: Long? = null,

    @Column(name = "staff_id")
    val staffId: Long? = null,

    @Column(name = "partition_area_id")
    val partitionAreaId: Long? = null,

    @Column(name = "row_version")
    val rowVersion: Long? = null,

    @Column(name = "plea_id")
    val pleaId: Long? = null,

    @Column(name = "remand_status_id")
    val remandStatusId: Long? = null,

    @Column(name = "created_by_user_id")
    val createdByUserId: Long? = null,

    @Column(name = "created_datetime")
    val createdDatetime: LocalDateTime? = null,

    @Column(name = "last_updated_user_id")
    val lastUpdatedUserId: Long? = null,

    @Column(name = "last_updated_datetime")
    val lastUpdatedDatetime: LocalDateTime? = null,

    @Column(name = "training_session_id")
    val trainingSessionId: Long? = null

) {
    fun isSentenceing(): Boolean {
        return appearanceType.code == "S"
    }
}

interface CourtAppearanceRepository : JpaRepository<CourtAppearance, Long> {

    fun findByPersonIdAndEventId(personId: Long, eventId: Long): List<CourtAppearance>
}

interface CourtReportRepository : JpaRepository<CourtReport, Long> {

    @Query(
        """
       select c from CourtReport c
       where c.courtAppearance.event = :event
    """
    )
    fun getAllByEvent(event: Event): List<CourtReport>

    @Query(
        """
        select courtReport 
        from CourtReport courtReport 
        where courtReport.personId = :personId 
        and courtReport.courtAppearance.event.id = :eventId
        and courtReport.softDeleted = false
    """
    )
    fun findByPersonIdAndEventId(personId: Long, eventId: Long): List<CourtReport>
}

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class Outcome(

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
) {
    enum class Code(val value: String, val description: String) {
        AWAITING_PSR("101", "Adjourned - Pre-Sentence Report")
    }
}

@Entity
@Table(name = "court_report")
@SQLRestriction("soft_deleted = 0")
class CourtReport(

    @Column(name = "offender_id")
    val personId: Long,
    @Column(name = "date_requested")
    val dateRequested: LocalDateTime,
    @Column(name = "date_required")
    val dateRequired: LocalDateTime,
    @Column(name = "completed_Date")
    val dateCompleted: LocalDateTime?,

    @Column(name = "allocation_date")
    val allocationDate: LocalDateTime? = null,

    @Column(name = "sent_to_court_date")
    val sentToCourtDate: LocalDateTime? = null,

    @Column(name = "received_by_court_date")
    val receivedByCourtDate: LocalDateTime? = null,

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
    val staff: Staff? = null,

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
