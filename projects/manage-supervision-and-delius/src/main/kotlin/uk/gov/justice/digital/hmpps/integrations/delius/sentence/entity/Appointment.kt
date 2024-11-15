package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import java.io.Serializable
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "contact")
@SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
@SQLRestriction("soft_deleted = 0")
class Appointment(
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime,

    @Column(name = "team_id")
    val teamId: Long,

    @Column(name = "staff_id")
    val staffId: Long,

    @Column(name = "last_updated_user_id")
    @LastModifiedBy
    var lastUpdatedUserId: Long,

    @Column(name = "contact_end_time")
    val endTime: ZonedDateTime?,

    val probationAreaId: Long? = null,

    val externalReference: String? = null,

    @Column(name = "description")
    val description: String? = null,

    @Column(name = "event_id")
    val eventId: Long? = null,

    @Column(name = "rqmnt_id")
    val rqmntId: Long? = null,

    val licConditionId: Long? = null,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Column(name = "created_by_user_id")
    val createdByUserId: Long? = null,

    @Column(name = "office_location_id")
    val officeLocationId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: ContactOutcome? = null,

    @Lob
    var notes: String? = null,

    @Column(name = "sensitive")
    @Convert(converter = YesNoConverter::class)
    var sensitive: Boolean? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_generator")
    @Column(name = "contact_id")
    val id: Long = 0
) {
    var partitionAreaId: Long = 0

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now()
}

interface AppointmentRepository : JpaRepository<Appointment, Long> {
    @Query(
        """
            select count(c.contact_id)
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            where c.offender_id = :personId and ct.attendance_contact = 'Y'
            and to_char(c.contact_date, 'YYYY-MM-DD') = :date
            and to_char(c.contact_start_time, 'HH24:MI') < :endTime 
            and to_char(c.contact_end_time, 'HH24:MI') > :startTime
            and c.soft_deleted = 0 and c.contact_outcome_type_id is null
        """,
        nativeQuery = true
    )
    fun getClashCount(
        personId: Long,
        date: String,
        startTime: String,
        endTime: String
    ): Int

}

fun AppointmentRepository.appointmentClashes(
    personId: Long,
    date: LocalDate,
    startTime: ZonedDateTime,
    endTime: ZonedDateTime,
): Boolean = getClashCount(
    personId,
    date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    startTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault())),
    endTime.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault()))
) > 0

interface AppointmentTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun AppointmentTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("AppointmentType", "code", code)

@Entity
@Table(name = "r_contact_type_outcome")
class ContactTypeOutcome(

    @EmbeddedId
    val id: ContactTypeOutcomeId,

    @ManyToOne
    @JoinColumn(name = "contact_type_id", insertable = false, updatable = false)
    val type: ContactType,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id", insertable = false, updatable = false)
    val outcome: ContactOutcome,
)

interface ContactTypeOutcomeRepository : JpaRepository<ContactTypeOutcome, ContactTypeOutcomeId> {
    fun findByIdContactTypeIdAndOutcomeCode(contactTypeId: Long, code: String): ContactTypeOutcome?
}

fun ContactTypeOutcomeRepository.getByTypeIdAndOutcomeCode(contactTypeId: Long, code: String) =
    findByIdContactTypeIdAndOutcomeCode(
        contactTypeId, code) ?: throw NotFoundException("ContactTypeOutcome", "contact_type_id $contactTypeId and outcome code", code)


@Embeddable
class ContactTypeOutcomeId(
    @Column(name = "contact_type_id")
    val contactTypeId: Long,

    @Column(name = "contact_outcome_type_id")
    val contactOutcomeTypeId: Long,

) : Serializable

