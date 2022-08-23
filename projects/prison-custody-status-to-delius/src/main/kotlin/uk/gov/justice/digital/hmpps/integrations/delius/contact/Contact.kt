package uk.gov.justice.digital.hmpps.integrations.delius.contact

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Version

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
    val date: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "contact_type_id", nullable = false)
    val type: ContactType,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @Lob
    @Column(name = "notes")
    val notes: String? = null,

    @Column(name = "staff_id")
    val staffId: Long,

    @Column(name = "team_id")
    val teamId: Long,

    @Column(name = "probation_area_id")
    val providerId: Long,

    // Removed the createdDate annotation as Delius relies on the created date time to infer the contact/recall linkage.
    // Please refer to: https://github.com/ministryofjustice/delius/blob/2e43abfe3110801bd1c3093bcde5fa001eae38e6/NDelius-ejb/src/main/java/uk/co/bconline/ndelius/service/throughcare/ThroughcareServiceBean.java#L1230-L1232
    @Column(name = "created_datetime", nullable = false)
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "created_by_user_id", nullable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(name = "last_updated_datetime", nullable = false)
    @LastModifiedDate
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "last_updated_user_id", nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(name = "trust_provider_flag", nullable = false)
    val trustProviderFlag: Long = 0,

    @Column(name = "staff_employee_id", nullable = false)
    val staffEmployeeId: Long = 1,

    @Column(name = "trust_provider_team_id", nullable = false)
    val teamProviderId: Long = 1,
)
