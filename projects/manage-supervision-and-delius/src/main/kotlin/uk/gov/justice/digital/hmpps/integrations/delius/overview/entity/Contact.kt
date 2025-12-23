package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.ContactDocument
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.entity.Team
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceCondition as LicenceConditionEntity

@Entity
@Table(name = "contact")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    val event: Event? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "contact_date")
    val date: LocalDate = LocalDate.now(),

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime? = ZonedDateTime.now(EuropeLondon),

    @Column(name = "rar_activity", length = 1)
    @Convert(converter = YesNoConverter::class)
    val rarActivity: Boolean? = null,

    @Column(name = "attended")
    @Convert(converter = YesNoConverter::class)
    val attended: Boolean? = null,

    @Column(name = "sensitive")
    @Convert(converter = YesNoConverter::class)
    val sensitive: Boolean? = null,

    @Column(name = "complied")
    @Convert(converter = YesNoConverter::class)
    val complied: Boolean? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: ContactOutcome? = null,

    @OneToMany(mappedBy = "contact", fetch = FetchType.LAZY)
    val documents: List<ContactDocument> = emptyList(),

    @ManyToOne
    @JoinColumn(name = "latest_enforcement_action_id", referencedColumnName = "enforcement_action_id")
    val action: EnforcementAction? = null,

    notes: String?,

    val nsiId: Long? = null,

    val description: String? = null,

    @ManyToOne
    @JoinColumn(name = "rqmnt_id")
    val requirement: Requirement? = null,

    @ManyToOne
    @JoinColumn(name = "lic_condition_id")
    val licenceCondition: LicenceConditionEntity? = null,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team? = null,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val location: OfficeLocation? = null,

    @Column(name = "contact_end_time")
    val endTime: ZonedDateTime? = null,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdByUserId: Long? = null,

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdated: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastUpdatedUserId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "last_updated_user_id", insertable = false, updatable = false)
    val lastUpdatedUser: User,

    @Column(name = "visor_contact")
    @Convert(converter = YesNoConverter::class)
    val isVisor: Boolean? = null,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "alert_active")
    var alert: Boolean? = false,

    val externalReference: String? = null,

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    val partitionAreaId: Long = 0
) {

    fun startDateTime(): ZonedDateTime =
        if (startTime != null) ZonedDateTime.of(date, startTime.toLocalTime(), EuropeLondon) else
            ZonedDateTime.of(date, date.atStartOfDay().toLocalTime(), EuropeLondon)

    fun endDateTime(): ZonedDateTime? =
        if (endTime != null) ZonedDateTime.of(date, endTime.toLocalTime(), EuropeLondon) else null

    fun hasARequiredOutcome(): Boolean? {
        if (type.contactOutcomeFlag != true) {
            return null
        }
        return outcome != null
    }

    fun isInitial(): Boolean = setOf(
        ContactTypeCode.INITIAL_APPOINTMENT_IN_OFFICE.value,
        ContactTypeCode.INITIAL_APPOINTMENT_ON_DOORSTEP.value,
        ContactTypeCode.INITIAL_APPOINTMENT_HOME_VISIT.value,
        ContactTypeCode.INITIAL_APPOINTMENT_BY_VIDEO.value
    ).contains(type.code)

    fun rescheduledStaff(): Boolean = setOf(
        ContactOutcomeTypeCode.RESCHEDULED.value,
        ContactOutcomeTypeCode.RESCHEDULED_SR.value
    ).contains(outcome?.code)

    fun rescheduledPop(): Boolean = setOf(
        ContactOutcomeTypeCode.RESCHEDULED.value,
        ContactOutcomeTypeCode.RESCHEDULED_POP.value
    ).contains(outcome?.code)

    fun rescheduled(): Boolean = rescheduledStaff() || rescheduledPop()
    fun isEmailOrTextFromPop(): Boolean = type.code == ContactTypeCode.EMAIL_OR_TEXT_FROM_POP.value
    fun isEmailOrTextToPop(): Boolean = type.code == ContactTypeCode.EMAIL_OR_TEXT_TO_POP.value
    fun isPhoneCallFromPop(): Boolean = type.code == ContactTypeCode.PHONE_CONTACT_FROM_POP.value
    fun isPhoneCallToPop(): Boolean = type.code == ContactTypeCode.PHONE_CONTACT_TO_POP.value
    fun isCommunication(): Boolean =
        type.categories.map { it.id.category.code }.contains(ContactCategoryCode.COMMUNICATION_CONTACT.value)

    @Lob
    var notes: String? = notes
        private set

    fun appendNotes(additionalNotes: String) {
        notes = notes?.plus(System.lineSeparator() + additionalNotes) ?: additionalNotes
    }

    companion object {
        val E_SUPERVISION_PREFIXES = setOf(
            "urn:uk:gov:hmpps:esupervision:check-in:",
            "urn:uk:gov:hmpps:esupervision:check-in-expiry:"
        )
    }
}

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    @Column
    val code: String,

    @Column(name = "attendance_contact")
    @Convert(converter = YesNoConverter::class)
    val attendanceContact: Boolean = false,

    @Column
    val description: String,

    @OneToMany(mappedBy = "id.contactTypeId", fetch = FetchType.LAZY)
    val categories: List<ContactCategory> = emptyList(),

    @Column(name = "sgc_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val systemGenerated: Boolean = false,

    @Column(name = "national_standards_contact")
    @Convert(converter = YesNoConverter::class)
    val nationalStandardsContact: Boolean = false,

    @Column(name = "contact_outcome_flag")
    @Convert(converter = YesNoConverter::class)
    val contactOutcomeFlag: Boolean? = false,

    @Column(name = "offender_event_0")
    @Convert(converter = YesNoConverter::class)
    val offenderContact: Boolean = false,

    @Column(name = "contact_location_flag", columnDefinition = "char(1)")
    val locationRequired: String,

    @Convert(converter = YesNoConverter::class)
    val editable: Boolean?,
)

interface ContactTypeRepository : CrudRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?

    @Query(
        """
            SELECT ct 
            FROM ContactType ct 
            WHERE ct.code in (:values)
            ORDER BY DECODE(ct.code, 'COAP',1,'COPT',2,'COVC',3,'COOO',4,'COAI',5,'CHVS',6,'C084',7,'CODC',8,'COSR',9)
        """
    )
    fun findByCodeIn(values: List<String>): List<ContactType>
}

fun ContactTypeRepository.getContactType(code: String) =
    findByCode(code) ?: throw NotFoundException("ContactType", "code", code)

@Immutable
@Entity
@Table(name = "r_contact_typecontact_category")
class ContactCategory(
    @EmbeddedId
    val id: ContactCategoryId,
)

@Embeddable
class ContactCategoryId(
    @Column(name = "contact_type_id")
    val contactTypeId: Long,

    @ManyToOne
    @JoinColumn(name = "standard_reference_list_id")
    val category: ReferenceData,
) : Serializable

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,

    val code: String,

    val description: String,

    @Column(name = "outcome_attendance")
    @Convert(converter = YesNoConverter::class)
    val outcomeAttendance: Boolean? = null,

    @Column(name = "outcome_compliant_acceptable")
    @Convert(converter = YesNoConverter::class)
    val outcomeCompliantAcceptable: Boolean? = null,

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    )

@Immutable
@Entity
@Table(name = "r_enforcement_action")
class EnforcementAction(
    val code: String,
    val description: String,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val contactType: ContactType,

    @Id
    @Column(name = "enforcement_action_id")
    val id: Long = 0
)

enum class ContactOutcomeTypeCode(val value: String) {
    RESCHEDULED("RSSR"),
    RESCHEDULED_SR("RSSL"),
    RESCHEDULED_POP("RSOF")
}

enum class ContactTypeCode(val value: String) {
    INITIAL_APPOINTMENT_IN_OFFICE("COAI"),
    INITIAL_APPOINTMENT_ON_DOORSTEP("CODI"),
    INITIAL_APPOINTMENT_HOME_VISIT("COHV"),
    INITIAL_APPOINTMENT_BY_VIDEO("COVI"),
    EMAIL_OR_TEXT_FROM_POP("CMOA"),
    EMAIL_OR_TEXT_TO_POP("CMOB"),
    PHONE_CONTACT_FROM_POP("CTOA"),
    PHONE_CONTACT_TO_POP("CTOB"),
}

enum class ContactCategoryCode(val value: String) {
    COMMUNICATION_CONTACT("LT")
}

interface ContactRepository : JpaRepository<Contact, Long> {

    @Query(
        """
        select c from Contact c
        left join fetch c.lastUpdatedUser u
        left join fetch u.staff st
        left join fetch st.provider prov
        left join fetch c.requirement rqmnt
        left join fetch rqmnt.mainCategory rmc
        left join fetch rmc.unitDetails ud
        left join fetch c.event e
        left join fetch c.type t
        left join fetch c.location l
        left join fetch l.ldu old
        left join fetch old.borough brgh
        left join fetch c.outcome o
        left join fetch t.categories cats
        left join fetch c.action a
        left join fetch a.contactType ct
        left join fetch e.disposal d
        left join fetch d.lengthUnit lu
        left join fetch d.terminationReason tr
        left join fetch e.court crt
        left join fetch d.type dt
        left join fetch e.mainOffence mo
        left join fetch mo.offence moo
        left join fetch rqmnt.subCategory rsc
        where c.person.id = :personId
        order by c.date desc, c.startTime desc 
    """
    )
    fun findByPersonId(personId: Long): List<Contact>

    fun findByPersonIdAndIdIn(personId: Long, ids: List<Long>): List<Contact>

    fun findByPersonIdAndEventIdIn(personId: Long, eventId: List<Long>): List<Contact>

    fun findByPersonIdAndId(personId: Long, id: Long): Contact?

    @Query(
        """
            select c.*
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            where c.offender_id = :personId 
            and ct.attendance_contact = 'Y'
            and (to_char(c.contact_date, 'YYYY-MM-DD') > :dateNow
            or (to_char(c.contact_date, 'YYYY-MM-DD') = :dateNow and to_char(c.contact_start_time, 'HH24:MI') > :timeNow))
            and c.soft_deleted = 0
        """,
        countQuery = """
            select count(1)
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            where c.offender_id = :personId 
            and ct.attendance_contact = 'Y'
            and (to_char(c.contact_date, 'YYYY-MM-DD') > :dateNow
            or (to_char(c.contact_date, 'YYYY-MM-DD') = :dateNow and to_char(c.contact_start_time, 'HH24:MI') > :timeNow))
            and c.soft_deleted = 0            
        """,
        nativeQuery = true
    )
    fun findUpComingAppointments(
        personId: Long,
        dateNow: String,
        timeNow: String,
        pageable: Pageable
    ): Page<Contact>

    @Query(
        """
            select c.*
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            where c.offender_id = :personId and ct.attendance_contact = 'Y'
            and (to_char(c.contact_date, 'YYYY-MM-DD') > :dateNow
            or (to_char(c.contact_date, 'YYYY-MM-DD') = :dateNow and to_char(c.contact_start_time, 'HH24:MI') > :timeNow))
            and c.contact_outcome_type_id is null and c.soft_deleted = 0
            order by c.contact_date, c.contact_start_time asc
            fetch first 1 row only
        """,
        nativeQuery = true
    )
    fun findFirstUpComingAppointment(
        personId: Long,
        dateNow: String,
        timeNow: String
    ): Contact?

    @Query(
        """
            select c.*
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            where c.offender_id = :personId and ct.attendance_contact = 'Y'
            and (to_char(c.contact_date, 'YYYY-MM-DD') < :dateNow
            or (to_char(c.contact_date, 'YYYY-MM-DD') = :dateNow and to_char(c.contact_start_time, 'HH24:MI') < :timeNow))
            and c.soft_deleted = 0
            order by c.contact_date, c.contact_start_time desc
        """,
        nativeQuery = true
    )
    fun findPreviousAppointments(
        personId: Long,
        dateNow: String,
        timeNow: String
    ): List<Contact>

    @Query(
        """
            select c.*
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            where c.offender_id = :personId 
            and ct.attendance_contact = 'Y'
            and (to_char(c.contact_date, 'YYYY-MM-DD') < :dateNow
            or (to_char(c.contact_date, 'YYYY-MM-DD') = :dateNow and to_char(c.contact_start_time, 'HH24:MI') < :timeNow))
            and c.soft_deleted = 0
        """,
        countQuery = """
            select count(*)
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            where c.offender_id = :personId 
            and ct.attendance_contact = 'Y'
            and (to_char(c.contact_date, 'YYYY-MM-DD') < :dateNow
            or (to_char(c.contact_date, 'YYYY-MM-DD') = :dateNow and to_char(c.contact_start_time, 'HH24:MI') < :timeNow))
            and c.soft_deleted = 0            
        """,
        nativeQuery = true
    )
    fun findPageablePreviousAppointments(
        personId: Long,
        dateNow: String,
        timeNow: String,
        pageable: Pageable
    ): Page<Contact>

    @Query(
        """
                select  o.first_name as forename,
                        o.second_name as second_name,
                        o.third_name as third_name,
                        o.surname as surname,
                        o.date_of_birth_date as dob,
                        c.contact_id as id,
                        o.crn as crn, 
                        ol.description as location, 
                        c.contact_date as contact_date, 
                        c.contact_start_time as contact_start_time,
                        c.contact_end_time as contact_end_time,
                        total_sentences,
                        rct.description as contactdescription,
                        rct.code as typecode,
                        case when c.complied = 'N' then 0 else 1 end as complied,
                        rtmc.code as rqmntmaincatcode,
                        nvl(rdt.description, latest_sentence_description)  as sentencedescription
                from contact c 
                join r_contact_type rct on rct.contact_type_id = c.contact_type_id 
                join offender o on o.offender_id = c.offender_id
                join staff s on s.staff_id = c.staff_id 
                join caseload cl on s.staff_id = cl.staff_employee_id and c.offender_id = cl.offender_id and (cl.role_code = 'OM') 
                left join office_location ol on ol.office_location_id = c.office_location_id 
                left join event e on e.event_id = c.event_id and (e.soft_deleted = 0) 
                left join disposal d on e.event_id = d.event_id 
                left join r_disposal_type rdt on rdt.disposal_type_id = d.disposal_type_id 
                left join rqmnt r on r.rqmnt_id = c.rqmnt_id
                left join r_rqmnt_type_main_category rtmc on rtmc.rqmnt_type_main_category_id = r.rqmnt_type_main_category_id
                left join ( 
                        select sub.* 
                        from
                          (select e.*,
                            rdt.description as latest_sentence_description,
                            count(e.event_id) over (partition by e.offender_id) as total_sentences,
                            row_number() over (partition by e.offender_id order by cast(e.event_number as NUMBER) desc) as row_num 
                            from event e 
                            join disposal d on d.event_id = e.event_id
                            join r_disposal_type rdt on rdt.disposal_type_id = d.disposal_type_id
                            where e.soft_deleted = 0 
                            and e.active_flag = 1
                            ) sub
                        where sub.row_num = 1
                 ) ls on ls.offender_id =c.offender_id 
                 where (c.soft_deleted = 0) 
                 and s.staff_id = :staffId
                 and rct.attendance_contact = 'Y' 
                 and (to_char(c.contact_date,'YYYY-MM-DD') > :dateNow  or (to_char(c.contact_date,'YYYY-MM-DD') = :dateNow 
                 and to_char(c.contact_start_time,'HH24:MI') > :timeNow)) 
        """,
        countQuery = """
                select count(1) 
                from contact c 
                join r_contact_type rct on rct.contact_type_id = c.contact_type_id 
                join offender o on o.offender_id = c.offender_id
                join staff s on s.staff_id = c.staff_id 
                join caseload cl on s.staff_id = cl.staff_employee_id and c.offender_id = cl.offender_id and (cl.role_code = 'OM')  
                where (c.soft_deleted = 0) 
                and s.staff_id = :staffId
                and rct.attendance_contact = 'Y' 
                and (to_char(c.contact_date,'YYYY-MM-DD') > :dateNow  or (to_char(c.contact_date,'YYYY-MM-DD') = :dateNow 
                and to_char(c.contact_start_time,'HH24:MI') > :timeNow)) 
        """,
        nativeQuery = true
    )
    fun findUpComingAppointmentsByUser(
        staffId: Long,
        dateNow: String,
        timeNow: String,
        pageable: Pageable
    ): Page<Appointment>

    @Query(
        """
            select  o.first_name as forename, 
                    o.second_name as second_name, 
                    o.third_name as third_name, 
                    o.surname as surname, 
                    o.date_of_birth_date as dob, 
                    c.contact_id as id, 
                    o.crn as crn, 
                    ol.description as location, 
                    c.contact_date as contact_date, 
                    c.contact_start_time as contact_start_time, 
                    c.contact_end_time as contact_end_time, 
                    (select count(1)
                         from event e 
                         join disposal d on d.event_id = e.event_id
                         where e.offender_id = o.offender_id
                         and e.active_flag = 1
                         and e.soft_deleted = 0) as totalsentences,
                    rct.description as contactdescription,
                    rct.code as typecode,
                    case when c.complied = 'N' then 0 else 1 end as complied,
                    rtmc.code as rqmntmaincatcode,
                    case when d.disposal_id is not null 
                    then 
                        rdt.description
                    else
                        (select rdt.description
                          from disposal d
                          join r_disposal_type rdt on rdt.disposal_type_id = d.disposal_type_id
                          where d.offender_id = o.offender_id
                          order by e.created_datetime desc fetch first 1 row only)
                    end as sentencedescription      
            from offender o
            join contact c on o.offender_id = c.offender_id
            join r_contact_type rct on rct.contact_type_id = c.contact_type_id
            join staff s on s.staff_id = c.staff_id
            join caseload cl on s.staff_id = cl.staff_employee_id and c.offender_id = cl.offender_id and (cl.role_code = 'OM')
            left join office_location ol on ol.office_location_id = c.office_location_id
            left join event e on e.event_id = c.event_id and e.active_flag = 1 and e.soft_deleted = 0
            left join disposal d on d.event_id = e.event_id
            left join r_disposal_type rdt on rdt.disposal_type_id = d.disposal_type_id
            left join rqmnt r on r.rqmnt_id = c.rqmnt_id
            left join r_rqmnt_type_main_category rtmc on rtmc.rqmnt_type_main_category_id = r.rqmnt_type_main_category_id
            where (c.soft_deleted = 0) 
            and s.staff_id = :staffId 
            and rct.attendance_contact = 'Y'  
            and rct.contact_outcome_flag = 'Y' 
            and c.contact_outcome_type_id is null 
            and (to_char(c.contact_date,'YYYY-MM-DD') < :dateNow or (to_char(c.contact_date,'YYYY-MM-DD') = :dateNow
            and to_char(c.contact_start_time,'HH24:MI') < :timeNow)) 
        """,
        nativeQuery = true,
        countQuery = """
            select  count(1)
            from offender o
            join contact c on o.offender_id = c.offender_id
            join r_contact_type rct on rct.contact_type_id = c.contact_type_id
            join staff s on s.staff_id = c.staff_id
            join caseload cl on s.staff_id = cl.staff_employee_id and c.offender_id = cl.offender_id and (cl.role_code = 'OM')
            where (c.soft_deleted = 0) 
            and s.staff_id = :staffId
            and rct.attendance_contact = 'Y' 
            and rct.contact_outcome_flag = 'Y'
            and c.contact_outcome_type_id is null
            and (to_char(c.contact_date,'YYYY-MM-DD') < :dateNow  or (to_char(c.contact_date,'YYYY-MM-DD') = :dateNow
            and to_char(c.contact_start_time,'HH24:MI') < :timeNow)) 
        """
    )
    fun findAppointmentsWithoutOutcomesByUser(
        staffId: Long,
        dateNow: String,
        timeNow: String,
        pageable: Pageable
    ): Page<Appointment>

    @Query(
        """
        with appt as ( select o.first_name,
                    o.second_name,
                    o.third_name,
                    o.surname,
                    o.date_of_birth_date,
                    c.contact_id,
                    o.crn,
                    c.contact_date,
                    c.contact_start_time,
                    c.contact_end_time,
                    rct.description,
                    rct.code,
                    c.rqmnt_id,
                    case when c.complied = 'N' then 0 else 1 end as complied
             from offender o
             join caseload cl on o.offender_id = cl.offender_id and (cl.role_code = 'OM')
             join contact c on c.offender_id = o.offender_id and c.staff_id = :staffId
             join r_contact_type rct on rct.contact_type_id = c.contact_type_id
             where cl.staff_employee_id = :staffId
               and rct.attendance_contact = 'Y'
               and rct.contact_outcome_flag = 'Y'
               and c.contact_outcome_type_id is null
               and c.soft_deleted = 0
               and (to_char(c.contact_date, 'YYYY-MM-DD') < :dateNow or
                    (to_char(c.contact_date, 'YYYY-MM-DD') = :dateNow and
                     to_char(c.contact_start_time, 'HH24:MI') < :timeNow)) ),
         rq as ( select r.rqmnt_id, rtmc.code
                 from rqmnt r
                 left join r_rqmnt_type_main_category rtmc
                           on rtmc.rqmnt_type_main_category_id = r.rqmnt_type_main_category_id )
        select appt.first_name         as forename,
               appt.second_name        as secondname,
               appt.third_name         as thirdname,
               appt.surname            as surname,
               appt.date_of_birth_date as dob,
               appt.contact_id         as id,
               appt.crn                as crn,
               appt.contact_date       as contact_date,
               appt.contact_start_time as contact_start_time,
               appt.contact_end_time   as contact_end_time,
               appt.description        as contactdescription,
               appt.code               as typecode,
               appt.complied           as complied,
               appt.code               as rqmntmaincatcode
        from appt
        left join rq on appt.rqmnt_id = rq.rqmnt_id   
    """,
        countQuery = """
        select count(1)
        from offender o
        join caseload cl on o.offender_id = cl.offender_id and (cl.role_code = 'OM')
        join contact c on c.offender_id = o.offender_id and c.staff_id = :staffId
        join r_contact_type rct on rct.contact_type_id = c.contact_type_id
        where cl.staff_employee_id = :staffId
        and rct.attendance_contact = 'Y'  
        and rct.contact_outcome_flag = 'Y'
        and c.contact_outcome_type_id is null 
        and c.soft_deleted = 0
        and (to_char(c.contact_date,'YYYY-MM-DD') < :dateNow
        or (to_char(c.contact_date,'YYYY-MM-DD') = :dateNow and to_char(c.contact_start_time,'HH24:MI') < :timeNow))              
        """,
        nativeQuery = true
    )
    fun findSummaryOfAppointmentsWithoutOutcomesByUser(
        staffId: Long,
        dateNow: String,
        timeNow: String,
        pageable: Pageable
    ): Page<Appointment>

    @Query(
        """
            SELECT c from ContactAlert ca
            join ca.contact c
            join OffenderManager com on com.person.id = c.person.id and com.active = true and com.softDeleted = false
            where c.alert = true and c.softDeleted = false
            and ca.staff.user.username = :username and com.staff.id = ca.staff.id
        """
    )
    fun findAllUserAlerts(username: String, pageable: Pageable): Page<Contact>
}

fun ContactRepository.getContact(id: Long) = findById(id).orElseThrow { NotFoundException("Contact", "id", id) }

fun ContactRepository.getFirstUpcomingAppointment(personId: Long, dateNow: String, timeNow: String) =
    findFirstUpComingAppointment(personId, dateNow, timeNow)

interface Appointment {
    val forename: String
    val secondName: String?
    val thirdName: String?
    val surname: String
    val dob: LocalDate
    val id: Long
    val crn: String
    val location: String?
    val contactDate: LocalDate
    val contactStartTime: LocalTime?
    val contactEndTime: LocalTime?
    val totalSentences: Int?
    val contactDescription: String
    val sentenceDescription: String?
    val typeCode: String
    val complied: Int?
    val rqmntMainCatCode: String?
}

fun ContactRepository.getContact(personId: Long, contactId: Long): Contact =
    findByPersonIdAndId(personId, contactId) ?: throw NotFoundException("Contact", "contactId", contactId)

fun ContactRepository.firstAppointment(
    personId: Long,
    date: LocalDate = LocalDate.now(),
    startTime: ZonedDateTime = ZonedDateTime.now(EuropeLondon)
): Contact? = getFirstUpcomingAppointment(
    personId,
    date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    startTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(EuropeLondon))
)

fun ContactRepository.getUpComingAppointments(
    personId: Long,
    pageable: Pageable,
    date: LocalDate = LocalDate.now(),
    startTime: ZonedDateTime = ZonedDateTime.now(EuropeLondon)
): Page<Contact> = findUpComingAppointments(
    personId = personId,
    dateNow = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    timeNow = startTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(EuropeLondon)),
    pageable
)

fun ContactRepository.getPreviousAppointments(
    personId: Long,
    date: LocalDate = LocalDate.now(),
    startTime: ZonedDateTime = ZonedDateTime.now(EuropeLondon)
): List<Contact> = findPreviousAppointments(
    personId = personId,
    dateNow = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    timeNow = startTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(EuropeLondon))
)

fun ContactRepository.getPageablePreviousAppointments(
    personId: Long,
    pageable: Pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "contact_date", "contact_start_time")),
    date: LocalDate = LocalDate.now(),
    startTime: ZonedDateTime = ZonedDateTime.now(EuropeLondon)
): Page<Contact> = findPageablePreviousAppointments(
    personId = personId,
    dateNow = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    timeNow = startTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(EuropeLondon)),
    pageable
)

@Immutable
@Entity
@Table(name = "office_location")
class OfficeLocation(

    @Column(name = "code", columnDefinition = "char(7)")
    val code: String,

    val description: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val townCity: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,

    @JoinColumn(name = "district_id")
    @ManyToOne
    val ldu: District,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @Id
    @Column(name = "office_location_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "district")
class District(

    @Column(name = "code")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Borough,

    @Id
    @Column(name = "district_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "borough")
class Borough(

    @Column(name = "code")
    val code: String,

    val description: String,

    @Id
    @Column(name = "borough_id")
    val id: Long
)