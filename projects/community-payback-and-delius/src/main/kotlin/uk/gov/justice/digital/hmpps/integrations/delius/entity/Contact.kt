package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@Entity
@Table(name = "contact")
@EntityListeners(AuditingEntityListener::class)
class Contact(
    @Id
    @Column(name = "contact_id")
    @SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedId(generator = "contact_id_seq")
    override val id: Long = 0,

    @Version
    @Column(name = "row_version")
    override var rowVersion: Long = 0,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val contactType: ContactType,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    var outcome: ContactOutcome? = null,

    @Convert(converter = YesNoConverter::class)
    var attended: Boolean? = null,

    @Convert(converter = YesNoConverter::class)
    var complied: Boolean? = null,

    @ManyToOne
    @JoinColumn(name = "latest_enforcement_action_id")
    val latestEnforcementAction: EnforcementAction? = null,

    @Column(name = "contact_date")
    var date: LocalDate,

    @Column(name = "contact_start_time")
    var startTime: LocalTime,

    @Column(name = "contact_end_time")
    var endTime: LocalTime? = null,

    val linkedContactId: Long? = null,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event? = null,

    @Column(name = "rqmnt_id")
    val requirementId: Long? = null,

    @Column(name = "lic_condition_id")
    val licenceConditionId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    var officeLocation: OfficeLocation? = null,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    var staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id")
    var team: Team,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    var provider: Provider,

    @Lob
    var notes: String?,

    @Convert(converter = YesNoConverter::class)
    var sensitive: Boolean? = false,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "alert_active")
    var alertActive: Boolean? = false,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastUpdatedUserId: Long = 0,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    // The following fields are not used, but must be set:
    val partitionAreaId: Long = 0,

    val trustProviderTeamId: Long = team.id,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val trustProviderFlag: Boolean = false,
) : Versioned

interface ContactRepository : JpaRepository<Contact, Long> {
    @Query(
        """
        select count(distinct c.date)
        from Contact c
        where c.event.id = :eventId
        and c.complied = false
        and c.contactType.nationalStandards = true
        and (:lastResetDate is null or c.date >= :lastResetDate)
        """
    )
    fun countFailureToComply(
        event: Event,
        eventId: Long = event.id,
        lastResetDate: LocalDate? = listOfNotNull(event.breachEnd, event.disposal?.date).maxOrNull()
    ): Long

    @Query(
        """
        select count(c.id) > 0 from Contact c
        where c.event.id = :eventId
        and c.contactType.code = :typeCode
        and c.outcome is null
        and (:since is null or c.date >= :since)
        """
    )
    fun enforcementReviewExists(
        eventId: Long,
        since: LocalDate?,
        typeCode: String = ContactType.Code.REVIEW_ENFORCEMENT_STATUS.value,
    ): Boolean
}

@Entity
@Table(name = "contact_alert")
class ContactAlert(
    @Id
    @SequenceGenerator(name = "contact_alert_id_generator", sequenceName = "contact_alert_id_seq", allocationSize = 1)
    @GeneratedId(generator = "contact_alert_id_generator")
    @Column(name = "contact_alert_id", nullable = false)
    val id: Long = 0,

    @Column(name = "contact_id")
    val contactId: Long?,

    @Column(name = "contact_type_id", nullable = false)
    val contactTypeId: Long,

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @Column(name = "offender_manager_id")
    val personManagerId: Long,

    @Column(name = "trust_provider_team_id")
    val teamId: Long,

    @Column(name = "staff_employee_id")
    val staffId: Long,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0
)

interface ContactAlertRepository : JpaRepository<ContactAlert, Long>