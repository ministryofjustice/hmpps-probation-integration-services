package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.ContactDocument
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Immutable
@Entity
@Table(name = "contact")
@SQLRestriction("soft_deleted = 0")
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long = 0,

    @Column(name = "offender_id")
    val personId: Long,

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

    @Lob
    val notes: String?,

    val nsiId: Long? = null,

    val description: String? = null,

    @ManyToOne
    @JoinColumn(name = "rqmnt_id")
    val requirement: Requirement? = null,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val location: OfficeLocation? = null,

    @Column(name = "contact_end_time")
    val endTime: ZonedDateTime? = null,

    @Column(name = "last_updated_datetime")
    val lastUpdated: ZonedDateTime,
//
//    @ManyToOne
//    @JoinColumn(name = "offender_id", insertable = false, updatable = false)
//    val latestSentence: LatestSentence? = null,

    @ManyToOne
    @JoinColumn(name = "last_updated_user_id")
    val lastUpdatedUser: User,

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
    val locationRequired: String
)

interface ContactTypeRepository : CrudRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?

    @Query(
        """
            SELECT ct 
            FROM ContactType ct 
            WHERE ct.code in (:values)
            ORDER BY DECODE(ct.code, 'C084',0,'CHVS',1,'COAI',2,'COSR',3,'COOO',4,'CODC',5,'COAP',6,'COPT',7,'COVC',8)
        """
    )
    fun findByCodeIn(values: List<String>, order: String): List<ContactType>
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
        where c.personId = :personId
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
            left join r_contact_type_outcome cot on cot.contact_outcome_type_id = c.contact_outcome_type_id
            where c.offender_id = :personId and ct.attendance_contact = 'Y'
            and (to_char(c.contact_date, 'YYYY-MM-DD') > :dateNow
            or (to_char(c.contact_date, 'YYYY-MM-DD') = :dateNow and to_char(c.contact_start_time, 'HH24:MI') > :timeNow))
            and c.soft_deleted = 0
            order by c.contact_date, c.contact_start_time asc
        """,
        nativeQuery = true
    )
    fun findUpComingAppointments(
        personId: Long,
        dateNow: String,
        timeNow: String,
    ): List<Contact>

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
                SELECT  o.first_name AS forename,
                        o.second_name AS second_name,
                        o.third_name AS third_name,
                        o.surname AS surname,
                        o.date_of_birth_date AS dob,
                        c.contact_id AS id,
                        o.crn AS crn, 
                        ol.description AS location, 
                        c.contact_date AS contact_date, 
                        c.contact_start_time AS contact_start_time,
                        c.contact_end_time AS contact_end_time,
                        total_sentences,
                        rct.description AS contactDescription,
                        NVL(rdt.description, latest_sentence_description)  AS sentenceDescription
                FROM contact c 
                JOIN r_contact_type rct ON rct.contact_type_id = c.contact_type_id 
                JOIN offender o ON o.offender_id = c.offender_id
                JOIN staff s ON s.staff_id = c.staff_id 
                JOIN caseload cl ON s.staff_id = cl.staff_employee_id AND c.offender_id = cl.offender_id AND (cl.role_code = 'OM') 
                LEFT JOIN office_location ol ON ol.office_location_id = c.office_location_id 
                LEFT JOIN event e ON e.event_id = c.event_id AND (e.soft_deleted = 0) 
                LEFT JOIN disposal d ON e.event_id = d.event_id 
                LEFT JOIN r_disposal_type rdt ON rdt.disposal_type_id = d.disposal_type_id 
                LEFT JOIN ( 
                        SELECT sub.* 
                        FROM
                          (SELECT e.*,
                            rdt.description AS latest_sentence_description,
                            COUNT(e.event_id) over (PARTITION BY e.offender_id) AS total_sentences,
                            ROW_NUMBER() over (PARTITION BY e.offender_id ORDER BY CAST(e.event_number AS NUMBER) DESC) AS row_num 
                            FROM event e 
                            JOIN disposal d ON d.event_id = e.event_id
                            JOIN r_disposal_type rdt ON rdt.disposal_type_id = d.disposal_type_id
                            WHERE e.soft_deleted = 0 
                            AND e.active_flag = 1
                            ) sub
                        WHERE sub.row_num = 1
                 ) ls ON ls.offender_id =c.offender_id 
                 WHERE (c.soft_deleted = 0) 
                 AND s.staff_id = :staffId
                 AND rct.attendance_contact = 'Y' 
                 AND (to_char(c.contact_date,'YYYY-MM-DD') > :dateNow  OR (to_char(c.contact_date,'YYYY-MM-DD') = :dateNow 
                 AND to_char(c.contact_start_time,'HH24:MI') > :timeNow)) 
        """,
        countQuery = """
                SELECT COUNT(1) 
                FROM contact c 
                JOIN r_contact_type rct ON rct.contact_type_id = c.contact_type_id 
                JOIN offender o ON o.offender_id = c.offender_id
                JOIN staff s ON s.staff_id = c.staff_id 
                JOIN caseload cl ON s.staff_id = cl.staff_employee_id AND c.offender_id = cl.offender_id AND (cl.role_code = 'OM')  
                WHERE (c.soft_deleted = 0) 
                AND s.staff_id = :staffId
                AND rct.attendance_contact = 'Y' 
                AND (to_char(c.contact_date,'YYYY-MM-DD') > :dateNow  OR (to_char(c.contact_date,'YYYY-MM-DD') = :dateNow 
                AND to_char(c.contact_start_time,'HH24:MI') > :timeNow)) 
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
            SELECT  o.first_name AS forename, 
                    o.second_name AS second_name, 
                    o.third_name AS third_name, 
                    o.surname AS surname, 
                    o.date_of_birth_date AS dob, 
                    c.contact_id AS id, 
                    o.crn AS crn, 
                    ol.description AS location, 
                    c.contact_date AS contact_date, 
                    c.contact_start_time AS contact_start_time, 
                    c.contact_end_time AS contact_end_time, 
                    (SELECT COUNT(1)
                         FROM event e 
                         JOIN disposal d ON d.event_id = e.event_id
                         WHERE e.offender_id = o.offender_id
                         AND e.active_flag = 1
                         AND e.soft_deleted = 0) as totalSentences,
                    rct.description AS contactDescription,
                    CASE WHEN d.disposal_id IS NOT NULL 
                    THEN 
                        rdt.description
                    ELSE
                        (SELECT rdt.description
                          FROM disposal d
                          JOIN r_disposal_type rdt ON rdt.disposal_type_id = d.disposal_type_id
                          WHERE d.offender_id = o.offender_id
                          ORDER BY e.created_datetime DESC FETCH FIRST 1 ROW ONLY)
                    END AS sentenceDescription      
            FROM offender o
            JOIN contact c ON o.offender_id = c.offender_id
            JOIN r_contact_type rct ON rct.contact_type_id = c.contact_type_id
            JOIN staff s ON s.staff_id = c.staff_id
            JOIN caseload cl ON s.staff_id = cl.staff_employee_id AND c.offender_id = cl.offender_id AND (cl.role_code = 'OM')
            LEFT JOIN office_location ol ON ol.office_location_id = c.office_location_id
            LEFT JOIN event e ON e.event_id = c.event_id AND e.ACTIVE_FLAG = 1 AND e.soft_deleted = 0
            LEFT JOIN disposal d ON d.event_id = e.event_id
            LEFT JOIN r_disposal_type rdt ON rdt.disposal_type_id = d.disposal_type_id
            WHERE (c.soft_deleted = 0) 
            AND s.staff_id = :staffId 
            AND rct.attendance_contact = 'Y'  
            AND rct.contact_outcome_flag = 'Y' 
            AND c.contact_outcome_type_id IS NULL 
            AND (to_char(c.contact_date,'YYYY-MM-DD') < :dateNow OR (to_char(c.contact_date,'YYYY-MM-DD') = :dateNow
            AND to_char(c.contact_start_time,'HH24:MI') < :timeNow)) 
        """,
        nativeQuery = true,
        countQuery = """
            SELECT  count(1)
            FROM offender o
            JOIN contact c ON o.offender_id = c.offender_id
            JOIN r_contact_type rct ON rct.contact_type_id = c.contact_type_id
            JOIN staff s ON s.staff_id = c.staff_id
            JOIN caseload cl ON s.staff_id = cl.staff_employee_id AND c.offender_id = cl.offender_id AND (cl.role_code = 'OM')
            WHERE (c.soft_deleted = 0) 
            AND s.staff_id = :staffId
            AND rct.attendance_contact = 'Y' 
            AND rct.contact_outcome_flag = 'Y'
            AND c.contact_outcome_type_id IS NULL
            AND (to_char(c.contact_date,'YYYY-MM-DD') < :dateNow  OR (to_char(c.contact_date,'YYYY-MM-DD') = :dateNow
            AND to_char(c.contact_start_time,'HH24:MI') < :timeNow)) 
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
        SELECT  o.first_name AS forename, 
                o.second_name AS secondName, 
                o.third_name AS thirdName, 
                o.surname AS surname, 
                o.date_of_birth_date AS dob,
                c.contact_id AS id,
                o.crn AS crn,
                c.contact_date AS contact_date, 
                c.contact_start_time AS contact_start_time, 
                c.contact_end_time AS contact_end_time,                                 
                rct.description AS contactDescription
        FROM offender o
        JOIN caseload cl ON o.offender_id = cl.offender_id AND (cl.role_code = 'OM')
        JOIN contact c ON c.offender_id = o.offender_id AND c.staff_id = :staffId
        JOIN r_contact_type rct ON rct.contact_type_id = c.contact_type_id
        WHERE cl.staff_employee_id = :staffId
        AND rct.attendance_contact = 'Y'  
        AND rct.contact_outcome_flag = 'Y'
        AND c.contact_outcome_type_id IS NULL 
        AND c.soft_deleted = 0
        AND (to_char(c.contact_date,'YYYY-MM-DD') < :dateNow
        OR (to_char(c.contact_date,'YYYY-MM-DD') = :dateNow AND to_char(c.contact_start_time,'HH24:MI') < :timeNow))    
    """,
        countQuery = """
        SELECT COUNT(1)
        FROM offender o
        JOIN caseload cl ON o.offender_id = cl.offender_id AND (cl.role_code = 'OM')
        JOIN CONTACT c ON c.offender_id = o.offender_id AND c.staff_id = :staffId
        JOIN r_contact_type rct ON rct.contact_type_id = c.contact_type_id
        WHERE cl.staff_employee_id = :staffId
        AND rct.attendance_contact = 'Y'  
        AND rct.contact_outcome_flag = 'Y'
        AND c.contact_outcome_type_id IS NULL 
        AND c.soft_deleted = 0
        AND (to_char(c.contact_date,'YYYY-MM-DD') < :dateNow
        OR (to_char(c.contact_date,'YYYY-MM-DD') = :dateNow AND to_char(c.contact_start_time,'HH24:MI') < :timeNow))              
        """,
        nativeQuery = true
    )
    fun findSummaryOfAppointmentsWithoutOutcomesByUser(
        staffId: Long,
        dateNow: String,
        timeNow: String,
        pageable: Pageable
    ): Page<Appointment>
}

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
    val contactStartTime: LocalTime
    val contactEndTime: LocalTime?
    val totalSentences: Int?
    val contactDescription: String
    val sentenceDescription: String?
}

fun ContactRepository.getContact(personId: Long, contactId: Long): Contact =
    findByPersonIdAndId(personId, contactId) ?: throw NotFoundException("Contact", "contactId", contactId)

fun ContactRepository.firstAppointment(
    personId: Long,
    date: LocalDate = LocalDate.now(),
    startTime: ZonedDateTime = ZonedDateTime.now(EuropeLondon)
): Contact? = findUpComingAppointments(
    personId,
    date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    startTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(EuropeLondon))
).firstOrNull()

fun ContactRepository.getUpComingAppointments(
    personId: Long,
    date: LocalDate = LocalDate.now(),
    startTime: ZonedDateTime = ZonedDateTime.now(EuropeLondon)
): List<Contact> = findUpComingAppointments(
    personId = personId,
    dateNow = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    timeNow = startTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(EuropeLondon)),
)

fun ContactRepository.getPreviousAppointments(
    personId: Long,
    date: LocalDate = LocalDate.now(),
    startTime: ZonedDateTime = ZonedDateTime.now(EuropeLondon)
): List<Contact> = findPreviousAppointments(
    personId = personId,
    dateNow = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    timeNow = startTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(EuropeLondon)),
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