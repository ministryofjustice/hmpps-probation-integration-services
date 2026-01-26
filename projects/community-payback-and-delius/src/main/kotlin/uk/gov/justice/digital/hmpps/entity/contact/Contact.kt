package uk.gov.justice.digital.hmpps.entity.contact

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.Versioned
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.staff.Provider
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

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

    @Column
    val externalReference: String? = null,

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

    @Column(name = "linked_contact_id")
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
) : Versioned {
    fun reference() = externalReference?.let { UUID.fromString(it.takeLast(36)) }
}

interface ContactRepository : JpaRepository<Contact, Long>