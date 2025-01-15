package uk.gov.justice.digital.hmpps.integrations.delius.contact.entity

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
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
    val date: ZonedDateTime,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "contact_type_id", nullable = false)
    val type: ContactType,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event? = null,

    @Column(name = "lic_condition_id")
    val licenceConditionId: Long? = null,

    @Column(name = "contact_outcome_type_id")
    val outcomeId: Long? = null,

    @Lob
    @Column
    val notes: String? = null,

    @Column
    val staffId: Long,

    @Column
    val teamId: Long,

    @Convert(converter = YesNoConverter::class)
    @Column(name = "alert_active")
    val alert: Boolean? = false,

    // Removed the createdDate annotation as Delius relies on the created date time to infer the contact/recall linkage.
    // Please refer to: https://github.com/ministryofjustice/delius/blob/2e43abfe3110801bd1c3093bcde5fa001eae38e6/NDelius-ejb/src/main/java/uk/co/bconline/ndelius/service/throughcare/ThroughcareServiceBean.java#L1230-L1232
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

interface ContactRepository : JpaRepository<Contact, Long> {
    fun deleteAllByLicenceConditionIdAndDateAfterAndOutcomeIdIsNull(
        licenceConditionId: Long,
        date: ZonedDateTime = ZonedDateTime.now()
    )
}
