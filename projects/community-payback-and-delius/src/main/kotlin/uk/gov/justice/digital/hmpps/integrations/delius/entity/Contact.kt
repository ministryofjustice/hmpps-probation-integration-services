package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "contact")
class Contact(
    @Id
    @Column(name = "contact_id")
    @SequenceGenerator(name = "contact_id_seq", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_seq")
    val id: Long = 0,

    @Column(name = "contact_type_id")
    val contactTypeId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    var contactOutcome: ContactOutcome? = null,

    @ManyToOne
    @JoinColumn(name = "latest_enforcement_action_id")
    val latestEnforcementAction: EnforcementAction? = null,

    @Column(name = "contact_date")
    var date: LocalDate,

    @Column(name = "contact_start_time")
    var startTime: LocalTime?,

    @Column(name = "contact_end_time")
    var endTime: LocalTime? = null,

    val linkedContactId: Long? = null,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "event_id")
    val eventId: Long? = null,

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
    var alertActive: Boolean? = false,

    @Version
    var rowVersion: Long = 0
)

interface ContactRepository : JpaRepository<Contact, Long>

@Entity
@Table(name = "contact_alert")
class ContactAlert(
    @Id
    @SequenceGenerator(name = "contact_alert_id_generator", sequenceName = "contact_alert_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_alert_id_generator")
    @Column(name = "contact_alert_id", nullable = false)
    val id: Long = 0,

    @Column
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
