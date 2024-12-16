package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.ContactDocument
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import java.io.Serializable
import java.time.LocalDate
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

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event? = null,

    @ManyToOne
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

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: ContactOutcome? = null,

    @OneToMany(mappedBy = "contact")
    val documents: List<ContactDocument> = emptyList(),

    @ManyToOne
    @JoinColumn(name = "latest_enforcement_action_id", referencedColumnName = "enforcement_action_id")
    val action: EnforcementAction? = null,

    @Lob
    val notes: String?,

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

    @OneToMany(mappedBy = "id.contactTypeId")
    val categories: List<ContactCategory> = emptyList(),

    @Column(name = "sgc_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val systemGenerated: Boolean = false,

    @Column(name = "national_standards_contact")
    @Convert(converter = YesNoConverter::class)
    val nationalStandardsContact: Boolean = false,
)

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
        where c.personId = :personId
        order by c.date desc, c.startTime desc 
    """
    )
    fun findByPersonId(personId: Long): List<Contact>

    fun findByPersonIdAndEventIdIn(personId: Long, eventId: List<Long>): List<Contact>

    fun findByPersonIdAndId(personId: Long, id: Long): Contact?

    @Query(
        """
            select c.*
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
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