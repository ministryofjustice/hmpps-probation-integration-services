package uk.gov.justice.digital.hmpps.appointments.domain.contact

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
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.appointments.domain.provider.Team
import uk.gov.justice.digital.hmpps.appointments.domain.provider.Location
import uk.gov.justice.digital.hmpps.appointments.domain.person.Person
import uk.gov.justice.digital.hmpps.appointments.domain.provider.Provider
import uk.gov.justice.digital.hmpps.appointments.domain.provider.Staff
import uk.gov.justice.digital.hmpps.appointments.domain.event.Event
import uk.gov.justice.digital.hmpps.appointments.domain.event.component.Requirement
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "contact")
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
open class Contact(
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event?,

    @ManyToOne
    @JoinColumn(name = "rqmnt_id")
    val requirement: Requirement?,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "contact_date")
    var date: LocalDate,

    @Column(name = "contact_start_time")
    var startTime: ZonedDateTime?,

    @Column(name = "contact_end_time")
    var endTime: ZonedDateTime?,

    @ManyToOne
    @JoinColumn(name = "team_id")
    var team: Team,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    var staff: Staff,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    var provider: Provider,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    var location: Location?,

    notes: String?,

    @Convert(converter = YesNoConverter::class)
    var sensitive: Boolean?,

    @Convert(converter = YesNoConverter::class)
    var rarActivity: Boolean?,

    outcome: ContactOutcome?,

    sendToVisor: Boolean?,

    val externalReference: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_generator")
    @Column(name = "contact_id")
    val id: Long = 0
) {
    var partitionAreaId: Long = 0

    @Version
    @Column(name = "row_version")
    val version: Long = 0

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdByUserId: Long? = null

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastUpdatedUserId: Long? = null

    @Column(name = "latest_enforcement_action_id")
    var enforcementActionId: Long? = null

    var enforcement: Boolean? = null

    var linkedContactId: Long? = null
        private set

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    var outcome: ContactOutcome? = outcome
        private set

    @Column(name = "complied")
    @Convert(converter = YesNoConverter::class)
    var complied: Boolean? = null
        private set

    @Lob
    var notes: String? = notes
        private set

    @Column(name = "visor_contact")
    @Convert(converter = YesNoConverter::class)
    var sendToVisor: Boolean? = sendToVisor
        set(value) {
            visorExported = if (value == true && visorExported == null) {
                false
            } else if (value != true) {
                null
            } else {
                visorExported
            }
            field = value
        }

    @Convert(converter = YesNoConverter::class)
    var visorExported: Boolean? = null
        private set

    fun appendNotes(parts: List<String>) =
        appendNotes(*parts.toTypedArray())

    fun appendNotes(vararg extraNotes: String) = apply {
        val lineBreak = System.lineSeparator() + System.lineSeparator()
        val appendable = extraNotes.filter { !it.isBlank() }
        if (appendable.isNotEmpty()) {
            val prefix = notes?.let { notes + lineBreak } ?: ""
            notes = appendable.joinToString(separator = lineBreak, prefix = prefix)
        }
    }

    fun applyOutcome(outcome: ContactOutcome) = apply {
        this.outcome = outcome
        this.complied = outcome.acceptable
    }

    fun linkTo(other: Contact) = apply {
        linkedContactId = other.id
    }
}

interface AppointmentRepository : JpaRepository<Contact, Long> {
    @Query(
        """
            select count(app.id) from Contact app 
            where app.event.id = :eventId and app.type.code = :contactCode 
            and app.outcome is null and (:breachEnd is null or app.date >= :breachEnd)
        """
    )
    fun countEnforcementUnderReview(
        @Param("eventId") eventId: Long,
        @Param("contactCode") contactCode: String,
        @Param("breachEnd") breachEnd: LocalDate?
    ): Long

    @Query(
        """
        select count(distinct app.date) 
        from Contact app 
        where app.event.id = :eventId and app.complied = false
        and app.type.nationalStandards = true 
        and (:lastResetDate is null or app.date >= :lastResetDate)
        """
    )
    fun countFailureToComply(eventId: Long, lastResetDate: LocalDate?): Long

    @Query(
        """
            select count(c.contact_id)
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            join offender p on p.offender_id = c.offender_id
            where p.crn = :personCrn and ct.attendance_contact = 'Y'
            and c.external_reference <> :externalReference
            and to_char(c.contact_date, 'YYYY-MM-DD') = :date
            and to_char(c.contact_start_time, 'HH24:MI') < :endTime 
            and to_char(c.contact_end_time, 'HH24:MI') > :startTime
            and c.soft_deleted = 0 and c.contact_outcome_type_id is null
        """,
        nativeQuery = true
    )
    fun getClashCount(
        personCrn: String,
        externalReference: String,
        date: String,
        startTime: String,
        endTime: String,
    ): Int

    fun findByExternalReference(externalReference: String): Contact?
}

fun AppointmentRepository.appointmentClashes(
    personCrn: String,
    externalReference: String,
    date: LocalDate,
    startTime: ZonedDateTime,
    endTime: ZonedDateTime,
): Boolean = getClashCount(
    personCrn,
    externalReference,
    date.format(ISO_LOCAL_DATE),
    startTime.format(ISO_LOCAL_TIME.withZone(EuropeLondon)),
    endTime.format(ISO_LOCAL_TIME.withZone(EuropeLondon)),
) > 0

fun AppointmentRepository.getAppointment(externalReference: String): Contact =
    findByExternalReference(externalReference)
        ?: throw NotFoundException("Appointment", "externalReference", externalReference)