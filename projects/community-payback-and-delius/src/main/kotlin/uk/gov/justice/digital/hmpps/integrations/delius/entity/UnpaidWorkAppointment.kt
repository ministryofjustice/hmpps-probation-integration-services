package uk.gov.justice.digital.hmpps.integrations.delius.entity

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "upw_appointment")
@Immutable
class UpwAppointment(
    @Id
    @Column(name = "upw_appointment_id")
    val id: Long,

    @Column(columnDefinition = "char(1)")
    val attended: String?,

    @Column(columnDefinition = "char(1)")
    val complied: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    val startTime: LocalTime,

    val endTime: LocalTime,

    val appointmentDate: LocalDate,

    val upwProjectId: Long,

    val upwDetailsId: Long,

    val contactId: Long,

    val contactOutcomeTypeId: Long?
)

@Entity
@Table(name = "upw_project")
@Immutable
class UpwProject(
    @Id
    @Column(name = "upw_project_id")
    val id: Long,

    val name: String,

    val code: String,

    val teamId: Long
)

@Entity
@Table(name = "upw_project_availability")
@Immutable
class UpwProjectAvailability(
    @Id
    @Column(name = "upw_project_availability_id")
    val id: Long,

    val upwProjectId: Long
)

@Entity
@Table(name = "upw_details")
@Immutable
class UpwDetails(
    @Id
    @Column(name = "upw_details_id")
    val id: Long,

    val disposalId: Long
)

@Entity
@Table(name = "disposal")
@Immutable
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

@Entity
@Table(name = "contact")
@Immutable
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long,

    val latestEnforcementActionId: Long?
)

@Entity
@Table(name = "r_enforcement_action")
@Immutable
class EnforcementAction(
    @Id
    @Column(name = "enforcement_action_id")
    val id: Long,

    @Convert(converter = YesNoConverter::class)
    val outstandingContactAction: Boolean
)

@JsonPropertyOrder(
    "projectId",
    "projectName",
    "projectCode",
    "startTime",
    "endTime",
    "appointmentDate",
    "allocatedCount",
    "outcomeCount",
    "enforcementActionCount"
)
interface UnpaidWorkSession{
    val projectId: Long
    val projectName: String
    val projectCode: String
    val startTime: LocalTime
    val endTime: LocalTime
    val appointmentDate: LocalDate
    val allocatedCount: Long
    val outcomeCount: Long
    val enforcementActionCount: Long
}

data class UnpaidWorkSessionDto(
    val projectId: Long,
    val projectName: String,
    val projectCode: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val appointmentDate: LocalDate,
    val allocatedCount: Long,
    val outcomeCount: Long,
    val enforcementActionCount: Long
)

fun UnpaidWorkSession.toDto() = UnpaidWorkSessionDto(
    projectId,
    projectName,
    projectCode,
    startTime,
    endTime,
    appointmentDate,
    allocatedCount,
    outcomeCount,
    enforcementActionCount
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
                and uwa1.appointment_date between trunc(CAST(:startDate AS DATE)) and trunc(CAST(:endDate AS DATE)) + (1 - 1 / 24 / 60 / 60)
                and uwa1.soft_deleted = 0 )
            --
            select uwp.upw_project_id as "projectId",
                   uwp.upw_project_name as "projectName",
                   uwp.upw_project_code as "projectCode",
                   uwa.start_time as "startTime",
                   uwa.end_time as "endTime",
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
            group by uwp.upw_project_id, uwp.upw_project_name, uwp.upw_project_code, uwa.start_time, uwa.end_time, uwa.appointment_date
            order by uwa.appointment_date asc, uwp.upw_project_name;
        """, nativeQuery = true
    )
    fun getUnpaidWorkSessionDetails(teamId: Long, startDate: LocalDate, endDate: LocalDate): List<UnpaidWorkSession>
}