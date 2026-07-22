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
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
import uk.gov.justice.digital.hmpps.utils.Extensions.allOfNotNull
import uk.gov.justice.digital.hmpps.utils.Extensions.filter
import uk.gov.justice.digital.hmpps.utils.Extensions.optionalFilter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "upw_appointment")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
@NamedEntityGraph(
    name = "UnpaidWorkAppointment.all",
    includeAllAttributes = true,
    attributeNodes = [
        NamedAttributeNode(value = "project", subgraph = "project"),
        NamedAttributeNode(value = "details", subgraph = "details"),
        NamedAttributeNode(value = "allocation", subgraph = "allocation"),
        NamedAttributeNode(value = "pickUpLocation"),
        NamedAttributeNode(value = "contact", subgraph = "contact"),
        NamedAttributeNode(value = "person"),
        NamedAttributeNode(value = "staff", subgraph = "staff"),
        NamedAttributeNode(value = "team", subgraph = "team"),
        NamedAttributeNode(value = "workQuality"),
        NamedAttributeNode(value = "behaviour"),
    ],
    subgraphs = [
        NamedSubgraph(
            name = "project",
            attributeNodes = [
                NamedAttributeNode(value = "team", subgraph = "team"),
                NamedAttributeNode(value = "projectType"),
                NamedAttributeNode(value = "placementAddress"),
                NamedAttributeNode(value = "beneficiaryContactAddress"),
            ]
        ),
        NamedSubgraph(
            name = "details",
            attributeNodes = [
                NamedAttributeNode(value = "disposal", subgraph = "disposal"),
                NamedAttributeNode(value = "status"),
            ]
        ),
        NamedSubgraph(
            name = "disposal",
            attributeNodes = [
                NamedAttributeNode(value = "event", subgraph = "event"),
                NamedAttributeNode(value = "type"),
            ]
        ),
        NamedSubgraph(
            name = "event",
            attributeNodes = [NamedAttributeNode(value = "court")]
        ),
        NamedSubgraph(
            name = "allocation",
            attributeNodes = [
                NamedAttributeNode(value = "details", subgraph = "details"),
                NamedAttributeNode(value = "project", subgraph = "project"),
                NamedAttributeNode(value = "projectAvailability", subgraph = "projectAvailability"),
                NamedAttributeNode(value = "allocationDay"),
                NamedAttributeNode(value = "requestedFrequency"),
                NamedAttributeNode(value = "pickUpLocation"),
            ]
        ),
        NamedSubgraph(
            name = "projectAvailability",
            attributeNodes = [
                NamedAttributeNode(value = "project"),
                NamedAttributeNode(value = "dayOfWeek"),
                NamedAttributeNode(value = "frequency"),
            ]
        ),
        NamedSubgraph(
            name = "contact",
            attributeNodes = [
                NamedAttributeNode(value = "contactType"),
                NamedAttributeNode(value = "outcome"),
                NamedAttributeNode(value = "event"),
                NamedAttributeNode(value = "latestEnforcementAction", subgraph = "enforcementAction"),
                NamedAttributeNode(value = "enforcement", subgraph = "enforcement"),
            ]
        ),
        NamedSubgraph(
            name = "enforcementAction",
            attributeNodes = [NamedAttributeNode(value = "contactType")]
        ),
        NamedSubgraph(
            name = "enforcement",
            attributeNodes = [NamedAttributeNode(value = "action")]
        ),
        NamedSubgraph(
            name = "staff",
            attributeNodes = [NamedAttributeNode(value = "grade")]
        ),
        NamedSubgraph(
            name = "team",
            attributeNodes = [NamedAttributeNode(value = "provider")]
        ),
    ]
)
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
    var date: LocalDate,

    @Column(name = "start_time")
    var startTime: LocalTime,

    @Column(name = "end_time")
    var endTime: LocalTime,

    @ManyToOne
    @JoinColumn(name = "upw_project_id")
    var project: UnpaidWorkProject,

    @ManyToOne
    @JoinColumn(name = "upw_details_id")
    val details: UnpaidWorkDetails,

    @ManyToOne
    @JoinColumn(name = "upw_allocation_id")
    val allocation: UnpaidWorkAllocation?,

    @Column(name = "pick_up_time")
    var pickUpTime: LocalTime?,

    @ManyToOne
    @JoinColumn(name = "pick_up_location_id")
    var pickUpLocation: OfficeLocation?,

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
    var team: Team,

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
        select * from (
            with uwp as ( select uwp1.upw_project_id, uwp1.name upw_project_name, uwp1.code upw_project_code
                          from upw_project uwp1,
                               upw_project_availability uwpav,
                               r_standard_reference_list type
                          where uwp1.team_id = :teamId
                            and (:typeCodesCount = 0 or type.code_value in (:typeCodes))
                            and uwpav.upw_project_id = uwp1.upw_project_id 
                            and type.standard_reference_list_id = uwp1.project_type_id),
                 uwa as ( select *
                          from upw_appointment uwa1
                          where uwa1.appointment_date between trunc(cast(:startDate as DATE)) and trunc(cast(:endDate as DATE)) + (1 - 1 / 24 / 60 / 60)
                            and uwa1.soft_deleted = 0 )
            select uwp.upw_project_id                                                                                as "projectId",
                   uwp.upw_project_name                                                                              as "projectName",
                   uwp.upw_project_code                                                                              as "projectCode",
                   count(distinct uwa.upw_details_id)                                                                as "allocatedCount",
                   uwa.appointment_date                                                                              as "appointmentDate",
                   count(distinct case when uwa.contact_outcome_type_id is not null
                                           then uwa.upw_appointment_id end)                                          as "outcomeCount",
                   count(distinct case when enf.outstanding_contact_action = 'Y'
                                           then uwa.upw_appointment_id end)                                          as "enforcementActionCount"
            from uwp
            join uwa on uwa.upw_project_id = uwp.upw_project_id
            join upw_details uwd on uwd.upw_details_id = uwa.upw_details_id
            join disposal d on d.disposal_id = uwd.disposal_id and d.soft_deleted = 0
            left join contact c on c.contact_id = uwa.contact_id
            left join r_enforcement_action enf on enf.enforcement_action_id = c.latest_enforcement_action_id
            group by uwp.upw_project_id, uwp.upw_project_name, uwp.upw_project_code, uwa.appointment_date
        ) sessions
        """,
        countQuery = """
        select count(*) from (
            with uwp as ( select uwp1.upw_project_id, uwp1.name upw_project_name, uwp1.code upw_project_code
                          from upw_project uwp1,
                               upw_project_availability uwpav,
                               r_standard_reference_list type
                          where uwp1.team_id = :teamId
                            and (:typeCodesCount = 0 or type.code_value in (:typeCodes))
                            and uwpav.upw_project_id = uwp1.upw_project_id 
                            and type.standard_reference_list_id = uwp1.project_type_id),
                 uwa as ( select *
                          from upw_appointment uwa1
                          where uwa1.appointment_date between trunc(cast(:startDate as DATE)) and trunc(cast(:endDate as DATE)) + (1 - 1 / 24 / 60 / 60)
                            and uwa1.soft_deleted = 0 )
            select uwp.upw_project_id   as "projectId",
                   uwp.upw_project_name as "projectName",
                   uwp.upw_project_code as "projectCode",
                   uwa.appointment_date as "enforcementActionCount"
            from uwp
            join uwa on uwa.upw_project_id = uwp.upw_project_id
            join upw_details uwd on uwd.upw_details_id = uwa.upw_details_id
            join disposal d on d.disposal_id = uwd.disposal_id and d.soft_deleted = 0
            group by uwp.upw_project_id, uwp.upw_project_name, uwp.upw_project_code, uwa.appointment_date
        ) sessions
        """,
        nativeQuery = true
    )
    fun getUnpaidWorkSessionDetails(
        teamId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        typeCodes: List<String>,
        pageable: Pageable,
        typeCodesCount: Int = typeCodes.count()
    ): Page<UnpaidWorkSessionDto>

    @Query(
        """
        select * from (
            with uwp as ( select uwp1.upw_project_id, uwp1.name upw_project_name, uwp1.code upw_project_code
                          from upw_project uwp1,
                               upw_project_availability uwpav,
                               r_standard_reference_list type
                          where uwp1.team_id in (:teamIds)
                            and (:typeCodesCount = 0 or type.code_value in (:typeCodes))
                            and uwpav.upw_project_id = uwp1.upw_project_id 
                            and type.standard_reference_list_id = uwp1.project_type_id),
                 uwa as ( select *
                          from upw_appointment uwa1
                          where uwa1.appointment_date between trunc(cast(:startDate as DATE)) and trunc(cast(:endDate as DATE)) + (1 - 1 / 24 / 60 / 60)
                            and uwa1.soft_deleted = 0 )
            select uwp.upw_project_id                                                                                as "projectId",
                   uwp.upw_project_name                                                                              as "projectName",
                   uwp.upw_project_code                                                                              as "projectCode",
                   count(distinct uwa.upw_details_id)                                                                as "allocatedCount",
                   uwa.appointment_date                                                                              as "appointmentDate",
                   count(distinct case when uwa.contact_outcome_type_id is not null
                                           then uwa.upw_appointment_id end)                                          as "outcomeCount",
                   count(distinct case when enf.outstanding_contact_action = 'Y'
                                           then uwa.upw_appointment_id end)                                          as "enforcementActionCount"
            from uwp
            join uwa on uwa.upw_project_id = uwp.upw_project_id
            join upw_details uwd on uwd.upw_details_id = uwa.upw_details_id
            join disposal d on d.disposal_id = uwd.disposal_id and d.soft_deleted = 0
            left join contact c on c.contact_id = uwa.contact_id
            left join r_enforcement_action enf on enf.enforcement_action_id = c.latest_enforcement_action_id
            group by uwp.upw_project_id, uwp.upw_project_name, uwp.upw_project_code, uwa.appointment_date
        ) sessions
        """,
        countQuery = """
        select count(*) from (
            with uwp as ( select uwp1.upw_project_id, uwp1.name upw_project_name, uwp1.code upw_project_code
                          from upw_project uwp1,
                               upw_project_availability uwpav,
                               r_standard_reference_list type
                          where uwp1.team_id in (:teamIds)
                            and (:typeCodesCount = 0 or type.code_value in (:typeCodes))
                            and uwpav.upw_project_id = uwp1.upw_project_id 
                            and type.standard_reference_list_id = uwp1.project_type_id),
                 uwa as ( select *
                          from upw_appointment uwa1
                          where uwa1.appointment_date between trunc(cast(:startDate as DATE)) and trunc(cast(:endDate as DATE)) + (1 - 1 / 24 / 60 / 60)
                            and uwa1.soft_deleted = 0 )
            select uwp.upw_project_id   as "projectId",
                   uwp.upw_project_name as "projectName",
                   uwp.upw_project_code as "projectCode",
                   uwa.appointment_date as "enforcementActionCount"
            from uwp
            join uwa on uwa.upw_project_id = uwp.upw_project_id
            join upw_details uwd on uwd.upw_details_id = uwa.upw_details_id
            join disposal d on d.disposal_id = uwd.disposal_id and d.soft_deleted = 0
            group by uwp.upw_project_id, uwp.upw_project_name, uwp.upw_project_code, uwa.appointment_date
        ) sessions
        """,
        nativeQuery = true
    )
    fun getUnpaidWorkSessionDetails(
        teamIds: List<Long>,
        startDate: LocalDate,
        endDate: LocalDate,
        typeCodes: List<String>,
        pageable: Pageable,
        typeCodesCount: Int = typeCodes.count()
    ): Page<UnpaidWorkSessionDto>

    @EntityGraph(value = "UnpaidWorkAppointment.all")
    fun getUpwAppointmentById(appointmentId: Long): UnpaidWorkAppointment?

    @EntityGraph(value = "UnpaidWorkAppointment.all")
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
                (select coalesce(sum(adjustment_amount), 0) from upw_adjustment where upw_adjustment.upw_details_id = upw_details.upw_details_id and adjustment_type = 'POSITIVE' and upw_adjustment.soft_deleted = 0) as positiveadjustments,
                (select coalesce(sum(adjustment_amount), 0) from upw_adjustment where upw_adjustment.upw_details_id = upw_details.upw_details_id and adjustment_type = 'NEGATIVE' and upw_adjustment.soft_deleted = 0) as negativeadjustments


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
    @EntityGraph(value = "UnpaidWorkAppointment.all")
    fun findByEventId(eventId: Long): List<UnpaidWorkAppointment>

    @EntityGraph(value = "UnpaidWorkAppointment.all")
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
            where trunc(cast(appointment_date as date)) + (cast(end_time as date) - trunc(cast(end_time as date))) between current_date - cast(:overdueDays as int) and current_date
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
        overdueDays: Int,
        pageable: Pageable,
        typeCodesCount: Int = typeCodes.count(),
    ): Page<Triple<Long, Int, Int>>

    @EntityGraph(value = "UnpaidWorkAppointment.all")
    fun findAll(spec: Specification<UnpaidWorkAppointment>, pageable: Pageable): Page<UnpaidWorkAppointment>

    fun findAppointments(
        crn: String?,
        eventNumber: String?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        projectCodes: List<String>?,
        projectTypeCodes: List<String>?,
        outcomeCodes: List<String>?,
        appointmentIds: List<Long>?,
        references: List<String>?,
        pageable: Pageable,
    ): Page<UnpaidWorkAppointment> = findAll(
        allOfNotNull(
            filter<String>("details.disposal.event.number") { it.isNotNull },
            optionalFilter("id", appointmentIds) { it.`in`(appointmentIds) },
            optionalFilter("contact.externalReference", references) { it.`in`(references) },
            optionalFilter("person.crn", crn) { it.equalTo(crn) },
            optionalFilter("details.disposal.event.number", eventNumber) { it.equalTo(eventNumber) },
            optionalFilter("date", fromDate) { greaterThanOrEqualTo(it, fromDate) },
            optionalFilter("date", toDate) { lessThanOrEqualTo(it, toDate) },
            optionalFilter("project.code", projectCodes) { it.`in`(projectCodes) },
            optionalFilter("project.projectType.code", projectTypeCodes) { it.`in`(projectTypeCodes) },
            optionalFilter("contact.outcome.code", outcomeCodes) { coalesce(it, "NO_OUTCOME").`in`(outcomeCodes) },
        ),
        pageable
    )
}

fun UnpaidWorkAppointmentRepository.getAppointment(id: Long) =
    getUpwAppointmentById(id) ?: throw NotFoundException("Unpaid Work Appointment", "id", id)
