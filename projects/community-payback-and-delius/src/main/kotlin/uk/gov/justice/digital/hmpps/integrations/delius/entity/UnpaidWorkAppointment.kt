package uk.gov.justice.digital.hmpps.integrations.delius.entity

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.AppointmentResponseCase
import uk.gov.justice.digital.hmpps.model.AppointmentResponseName
import uk.gov.justice.digital.hmpps.model.CodeDescription
import uk.gov.justice.digital.hmpps.service.CaseAccess
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@Entity
@Table(name = "upw_appointment")
@SQLRestriction("soft_deleted = 0")
class UpwAppointment(
    @Id
    @SequenceGenerator(
        name = "upw_appointment_id_generator",
        sequenceName = "upw_appointment_id_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "upw_appointment_id_generator")
    @Column(name = "upw_appointment_id")
    val id: Long,

    @Column(name = "attended")
    @Convert(converter = YesNoConverter::class)
    var attended: Boolean?,

    @Column(name = "complied")
    @Convert(converter = YesNoConverter::class)
    var complied: Boolean?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    var startTime: LocalTime,

    var endTime: LocalTime,

    @Column(name = "appointment_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "upw_project_id")
    val project: UpwProject,

    @ManyToOne
    @JoinColumn(name = "upw_details_id")
    val details: UpwDetails,

    @ManyToOne
    @JoinColumn(name = "pick_up_location_id")
    val pickUpLocation: OfficeLocation,

    val pickUpTime: LocalTime?,

    var penaltyTime: Long?, // In minutes

    @ManyToOne
    @JoinColumn(name = "contact_id")
    val contact: Contact,

    var contactOutcomeTypeId: Long?,

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

    var minutesCredited: Long? = null,

    @Version
    var rowVersion: Long,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
)

fun UpwAppointment.toAppointmentResponseCase(
    limitedAccess: CaseAccess
) = AppointmentResponseCase(
    crn = this.person.crn,
    name = AppointmentResponseName(
        forename = this.person.forename,
        surname = this.person.surname,
        middleNames = this.person.secondName?.let { names -> listOf(names) } ?: emptyList()
    ),
    dateOfBirth = this.person.dateOfBirth,
    currentExclusion = limitedAccess.userExcluded,
    exclusionMessage = limitedAccess.exclusionMessage,
    currentRestriction = limitedAccess.userRestricted,
    restrictionMessage = limitedAccess.restrictionMessage,
)

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class UpwDetails(
    @Id
    @Column(name = "upw_details_id")
    val id: Long,

    val disposalId: Long,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

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
    val appointmentDate: LocalDate
    val allocatedCount: Long
    val outcomeCount: Long
    val enforcementActionCount: Long

    fun toModel() = UnpaidWorkSession(
        CodeDescription(projectCode, projectName),
        appointmentDate,
        allocatedCount,
        outcomeCount,
        enforcementActionCount
    )
}

data class UnpaidWorkSession(
    val project: CodeDescription,
    val date: LocalDate,
    val allocatedCount: Long,
    val outcomeCount: Long,
    val enforcementActionCount: Long
)

interface UpwMinutesDto {
    val id: Long
    val requiredMinutes: Long
    val completedMinutes: Long

    fun toModel() = UpwMinutes(requiredMinutes, completedMinutes)
}

data class UpwMinutes(
    val requiredMinutes: Long,
    val completedMinutes: Long
)

interface UnpaidWorkAppointmentRepository : JpaRepository<UpwAppointment, Long> {
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

    fun getUpwAppointmentById(appointmentId: Long): UpwAppointment?

    fun findByDateAndProjectCodeAndDetailsSoftDeletedFalse(
        appointmentDate: LocalDate,
        projectCode: String
    ): List<UpwAppointment>

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
                as "completedMinutes"
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
}

fun UnpaidWorkAppointmentRepository.getAppointment(id: Long) =
    getUpwAppointmentById(id) ?: throw NotFoundException("Unpaid Work Appointment", "id", id)