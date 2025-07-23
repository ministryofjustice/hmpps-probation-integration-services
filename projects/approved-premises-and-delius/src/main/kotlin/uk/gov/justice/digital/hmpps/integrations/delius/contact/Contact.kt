package uk.gov.justice.digital.hmpps.integrations.delius.contact

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.contact.outcome.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Contact(
    @Id
    @SequenceGenerator(name = "contact_id_generator", sequenceName = "contact_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_id_generator")
    @Column(name = "contact_id", nullable = false)
    val id: Long = 0,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @Column(name = "contact_date", nullable = false)
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime?,

    @ManyToOne
    @JoinColumn(name = "contact_type_id", nullable = false)
    val type: ContactType,

    @Column(name = "description")
    val description: String? = null,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @Column(name = "event_id")
    val eventId: Long? = null,

    @Column(name = "nsi_id")
    val nsiId: Long? = null,

    @Column
    val primaryKeyId: Long? = null,

    @Column
    val tableName: String? = null,

    @Lob
    @Column
    val notes: String? = null,

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @Column(name = "office_location_id")
    val locationId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcome: ContactOutcome? = null,

    @Column(name = "alert_active")
    @Convert(converter = YesNoConverter::class)
    val alert: Boolean? = false,

    var externalReference: String? = null,

    @CreatedDate
    @Column(nullable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(nullable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(nullable = false)
    val partitionAreaId: Long = 0
)
