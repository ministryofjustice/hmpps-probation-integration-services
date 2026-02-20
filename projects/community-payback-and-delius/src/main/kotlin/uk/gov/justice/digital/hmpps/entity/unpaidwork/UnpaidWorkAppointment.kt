package uk.gov.justice.digital.hmpps.entity.unpaidwork

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.Versioned
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.CodeDescription
import uk.gov.justice.digital.hmpps.model.RequirementProgress
import uk.gov.justice.digital.hmpps.model.Session
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "upw_appointment")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
class UnpaidWorkAppointment(
    @Id
    @Column(name = "upw_appointment_id")
    override val id: Long,

    @Column(name = "attended")
    @Convert(converter = YesNoConverter::class)
    var attended: Boolean?,

    @Column(name = "complied")
    @Convert(converter = YesNoConverter::class)
    var complied: Boolean?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "appointment_date")
    val date: LocalDate,

    @Column(name = "start_time")
    var startTime: LocalTime,

    @Column(name = "end_time")
    var endTime: LocalTime,

    @ManyToOne
    @JoinColumn(name = "upw_project_id")
    val project: UnpaidWorkProject,

    @ManyToOne
    @JoinColumn(name = "upw_details_id")
    val details: UnpaidWorkDetails,

    @ManyToOne
    @JoinColumn(name = "upw_allocation_id")
    val allocation: UnpaidWorkAllocation?,

    @Column(name = "pick_up_time")
    val pickUpTime: LocalTime?,

    @ManyToOne
    @JoinColumn(name = "pick_up_location_id")
    val pickUpLocation: OfficeLocation?,

    @Column(name = "penalty_time")
    var penaltyMinutes: Long?,

    @ManyToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    @Column(name = "contact_outcome_type_id")
    var outcomeId: Long? = contact.outcome?.id,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    var staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

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

    @Column(name = "minutes_credited")
    var minutesCredited: Long? = null,

    @Column(name = "minutes_offered")
    val minutesOffered: Long? = null,

    @Lob
    var notes: String?,

    @Version
    override var rowVersion: Long = 0,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(name = "partition_area_id")
    val partitionAreaId: Long = 0,
) : Versioned {
    fun version() = UUID(rowVersion, contact.rowVersion)
}

@JsonPropertyOrder(
    "projectId",
    "projectName",
    "projectCode",
    "appointmentDate",
    "allocatedCount",
    "outcomeCount",
    "enforcementActionCount"
)
interface UnpaidWorkSessionDto {
    val projectId: Long
    val projectName: String
    val projectCode: String
    val appointmentDate: LocalDateTime
    val allocatedCount: Long
    val outcomeCount: Long
    val enforcementActionCount: Long

    fun toModel() = Session(
        CodeDescription(projectCode, projectName),
        appointmentDate.toLocalDate(),
        allocatedCount,
        outcomeCount,
        enforcementActionCount
    )
}

interface UpwMinutesDto {
    val id: Long
    val requiredMinutes: Long
    val completedMinutes: Long
    val positiveAdjustments: Long
    val negativeAdjustments: Long

    fun toModel() = RequirementProgress(requiredMinutes, completedMinutes, positiveAdjustments - negativeAdjustments)
}

interface UnpaidWorkAppointmentRepository : JpaRepository<UnpaidWorkAppointment, Long> {
    @Query(
        """
            with uwp as ( select uwp1.upw_project_id, uwp1.name upw_project_name, uwp1.code upw_project_code
              from upw_project uwp1,
                   upw_project_availability uwpav
              where 1 = 1
                and uwp1.team_id = :teamId
                and uwpav.upw_project_id = uwp1.upw_project_id ),
     uwa as ( select uwa1.upw_appointment_id,
                     uwa1.upw_details_id,
                     uwa1.upw_project_id,
                     uwa1.start_time,
                     uwa1.end_time,
                     uwa1.appointment_date,
                     uwa1.contact_id,
                     uwa1.contact_outcome_type_id
              from upw_appointment uwa1
              where 1 = 1
                and uwa1.appointment_date between trunc(cast(:startDate as DATE)) and trunc(cast(:endDate as DATE)) + (1 - 1 / 24 / 60 / 60)
                and uwa1.soft_deleted = 0 )
            --
            select uwp.upw_project_id as "projectId",
                   uwp.upw_project_name as "projectName",
                   uwp.upw_project_code as "projectCode",
                   count(distinct uwa.upw_details_id) as "allocatedCount",
                   uwa.appointment_date as "appointmentDate",
                   count(distinct case when uwa.contact_outcome_type_id is not null then uwa.upw_appointment_id end) as "outcomeCount",
                   count(distinct case when enf.outstanding_contact_action = 'Y' then uwa.upw_appointment_id end)    as "enforcementActionCount"
                from uwp
                join uwa on uwa.upw_project_id = uwp.upw_project_id
                join upw_details uwd on uwd.upw_details_id = uwa.upw_details_id
                join disposal d on d.disposal_id = uwd.disposal_id and d.soft_deleted = 0
                left join "CONTACT" c on c.contact_id = uwa.contact_id
                left join r_enforcement_action enf 
                       on enf.enforcement_action_id = c.latest_enforcement_action_id 
            group by uwp.upw_project_id, uwp.upw_project_name, uwp.upw_project_code, uwa.appointment_date
            order by uwa.appointment_date asc, uwp.upw_project_name
        """, nativeQuery = true
    )
    fun getUnpaidWorkSessionDetails(teamId: Long, startDate: LocalDate, endDate: LocalDate): List<UnpaidWorkSessionDto>

    fun getUpwAppointmentById(appointmentId: Long): UnpaidWorkAppointment?

    fun findByDateAndProjectCodeAndDetailsSoftDeletedFalse(
        appointmentDate: LocalDate,
        projectCode: String
    ): List<UnpaidWorkAppointment>

    @Query(
        """
        select
            upw_details.upw_details_id as "id",
            case
                when r_disposal_type.pre_cja2003 = 'Y' then disposal.length * 60
                else coalesce(
                        (select sum(rqmnt.length) * 60 from rqmnt rqmnt
                         join r_rqmnt_type_main_category on r_rqmnt_type_main_category.rqmnt_type_main_category_id = rqmnt.rqmnt_type_main_category_id and r_rqmnt_type_main_category.code = 'W'
                         where rqmnt.disposal_id = disposal.disposal_id and rqmnt.soft_deleted = 0),
                        0)
                end as "requiredMinutes",
            coalesce(
                    (select sum(appts.minutes_credited) from upw_appointment appts where appts.upw_details_id = upw_details.upw_details_id and appts.soft_deleted = 0),
                    0)
                as "completedMinutes",
                (select coalesce(sum(adjustment_amount), 0) from upw_adjustment where upw_adjustment.upw_details_id = upw_details.upw_details_id and adjustment_type = 'POSITIVE' and upw_adjustment.soft_deleted = 0) as positiveAdjustments,
                (select coalesce(sum(adjustment_amount), 0) from upw_adjustment where upw_adjustment.upw_details_id = upw_details.upw_details_id and adjustment_type = 'NEGATIVE' and upw_adjustment.soft_deleted = 0) as negativeAdjustments
            
                
        from upw_details
        join disposal
             on disposal.disposal_id = upw_details.disposal_id
        join r_disposal_type
             on r_disposal_type.disposal_type_id = disposal.disposal_type_id
        where upw_details.soft_deleted = 0
          and upw_details.upw_details_id in :upwDetailsId
    """, nativeQuery = true
    )
    fun getUpwRequiredAndCompletedMinutes(upwDetailsId: List<Long>): List<UpwMinutesDto>

    @Query(
        """
        select a from UnpaidWorkAppointment a
        where a.details.disposal.event.id = :eventId
        and a.softDeleted = false
        and a.details.softDeleted = false
        and a.details.disposal.softDeleted = false
    """
    )
    fun findByEventId(eventId: Long): List<UnpaidWorkAppointment>

    fun findByDetailsDisposalEventIdInAndProjectProjectTypeCodeIn(
        eventIds: Collection<Long>,
        projectTypeCodes: Collection<String>
    ): List<UnpaidWorkAppointment>

    @Query(
        """
        select 
            project.upw_project_id, 
            coalesce(overdue_count, 0) as overdue_count, 
            coalesce(overdue_days, 0) as overdue_days
        from upw_project project
        join team t on t.team_id = project.team_id
        join r_standard_reference_list project_type on project_type.standard_reference_list_id = project.project_type_id
        left join (
            select 
                upw_project_id, 
                count(*) as overdue_count, 
                cast(max(trunc(cast(current_date as date)) - trunc(cast(appointment_date as date))) as int) as overdue_days
            from upw_appointment
            where trunc(cast(appointment_date as date)) + (end_time - trunc(cast(end_time as date))) between current_date - 45 and current_date
              and contact_outcome_type_id is null
              and soft_deleted = 0
            group by upw_project_id
        ) appointment_stats on appointment_stats.upw_project_id = project.upw_project_id
        where t.code = :teamCode
        and (:typeCodesCount = 0 or project_type.code_value in (:typeCodes))
        and (project.completion_date is null or project.completion_date > current_date)
        """,
        nativeQuery = true
    )
    fun getOutcomeStats(
        teamCode: String,
        typeCodes: List<String>,
        pageable: Pageable,
        typeCodesCount: Int = typeCodes.count()
    ): Page<Triple<Long, Int, Int>>

    @Query(
        """
        select a from UnpaidWorkAppointment a
        where (:crn is null or a.person.crn = :crn)
          and (:fromDate is null or a.date >= :fromDate)
          and (:toDate is null or a.date <= :toDate)
          and (:projectCodes is null or a.project.code in :projectCodes)
          and (:projectTypeCodes is null or a.project.projectType.code in :projectTypeCodes)
          and (:outcomeCodes is null or a.contact.outcome.code in :outcomeCodes)
    """
    )
    fun findAppointments(
        @Param("crn") crn: String?,
        @Param("fromDate") fromDate: LocalDate?,
        @Param("toDate") toDate: LocalDate?,
        @Param("projectCodes") projectCodes: List<String>?,
        @Param("projectTypeCodes") projectTypeCodes: List<String>?,
        @Param("outcomeCodes") outcomeCodes: List<String>?,
        pageable: Pageable
    ): Page<UnpaidWorkAppointment>
}

fun UnpaidWorkAppointmentRepository.getAppointment(id: Long) =
    getUpwAppointmentById(id) ?: throw NotFoundException("Unpaid Work Appointment", "id", id)
