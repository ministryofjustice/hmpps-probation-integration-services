package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "court_appearance")
@SequenceGenerator(name = "court_appearance_id_seq", sequenceName = "court_appearance_id_seq", allocationSize = 1)
class CourtAppearance(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "court_appearance_id_seq")
    @Column(name = "court_appearance_id")
    val id: Long? = null,

    @Column(name = "appearance_date")
    val appearanceDate: LocalDate,

    @Column(name = "crown_court_calendar_number")
    val crownCourtCalendarNumber: String? = null,

    @Column(name = "bail_conditions")
    val bailConditions: String? = null,

    @Column(name = "court_notes", columnDefinition = "clob")
    val courtNotes: String? = null,

    @JoinColumn(name = "event_id")
    @ManyToOne
    val event: Event,

    @Column(name = "team_id")
    val teamId: Long? = null,

    @Column(name = "staff_id")
    val staffId: Long? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @Column
    val partitionAreaId: Long = 0L,

    @ManyToOne
    @JoinColumn(name = "court_id")
    val court: Court,

    @Version
    @Column(name = "row_version")
    var version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "appearance_type_id")
    val appearanceType: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "plea_id")
    val plea: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "outcome_id")
    val outcome: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "remand_status_id")
    val remandStatus: ReferenceData? = null,

    @Column(nullable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(name = "last_updated_user_id")
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @CreatedDate
    @Column(nullable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "training_session_id")
    val trainingSessionId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
)

interface CourtAppearanceRepository : JpaRepository<CourtAppearance, Long> {
    @Query(
        """
        SELECT c FROM CourtAppearance c
        WHERE c.crownCourtCalendarNumber = :crownCourtCalendarNumber
        ORDER BY c.appearanceDate DESC
    """
    )
    fun findLatestByCaseUrn(crownCourtCalendarNumber: String): CourtAppearance?
}