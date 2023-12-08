package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Version
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime

interface ManagementTierEventRepository : JpaRepository<ManagementTierEvent, Long>

@Entity
@EntityListeners(AuditingEntityListener::class)
class ManagementTierEvent(
    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,
    @ManyToOne
    @JoinColumn(name = "contact_type_id", nullable = false)
    val contactType: ContactType,
    @ManyToOne
    @JoinColumn(name = "tier_change_reason_id")
    val changeReason: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "tier_id")
    val tier: ReferenceData,
    @Id
    @SequenceGenerator(
        name = "management_tier_event_id_generator",
        sequenceName = "management_tier_event_id_seq",
        allocationSize = 1,
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "management_tier_event_id_generator")
    @Column(name = "management_tier_event_id", nullable = false)
    val id: Long = 0,
    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),
    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,
    @CreatedDate
    @Column(nullable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),
    @Column(nullable = false)
    @CreatedBy
    var createdByUserId: Long = 0,
    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,
)
