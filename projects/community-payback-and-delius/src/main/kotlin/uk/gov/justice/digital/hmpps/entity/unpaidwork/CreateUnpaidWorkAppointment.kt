package uk.gov.justice.digital.hmpps.entity.unpaidwork

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.Versioned
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Entity
@Table(name = "upw_appointment")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
class CreateUnpaidWorkAppointment(
    @Id
    @SequenceGenerator(
        name = "upw_appointment_id_generator",
        sequenceName = "upw_appointment_id_seq",
        allocationSize = 1
    )
    @GeneratedId(generator = "upw_appointment_id_generator")
    @Column(name = "upw_appointment_id")
    override val id: Long? = null,

    @Version
    override var rowVersion: Long = 0,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "contact_id")
    val contactId: Long,

    @ManyToOne
    @JoinColumn(name = "upw_project_id")
    val project: UpwProject,

    @ManyToOne
    @JoinColumn(name = "upw_details_id")
    val details: UnpaidWorkDetails,

    @Column(name = "upw_allocation_id")
    val allocationId: Long?,

    @Column(name = "appointment_date")
    val date: LocalDate,

    @Column(name = "start_time")
    var startTime: LocalTime,

    @Column(name = "end_time")
    var endTime: LocalTime,

    @Column(name = "pick_up_time")
    val pickUpTime: LocalTime?,

    @ManyToOne
    @JoinColumn(name = "pick_up_location_id")
    val pickUpLocation: OfficeLocation?,

    @Column(name = "contact_outcome_type_id")
    var outcomeId: Long?,

    @Column(name = "minutes_offered")
    val minutesOffered: Long? = ChronoUnit.MINUTES.between(startTime, endTime),

    @Column(name = "minutes_credited")
    var minutesCredited: Long?,

    @Column(name = "penalty_time")
    var penaltyMinutes: Long?,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "high_visibility_vest")
    var hiVisWorn: Boolean?,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "intensive")
    var workedIntensively: Boolean?,

    @ManyToOne
    @JoinColumn(name = "work_quality_id")
    var workQuality: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "behaviour_id")
    var behaviour: ReferenceData?,

    @Column(name = "attended")
    @Convert(converter = YesNoConverter::class)
    var attended: Boolean?,

    @Column(name = "complied")
    @Convert(converter = YesNoConverter::class)
    var complied: Boolean?,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    var staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team = project.team,

    @Lob
    var notes: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0
) : Versioned

interface CreateUnpaidWorkAppointmentRepository : JpaRepository<CreateUnpaidWorkAppointment, Long>