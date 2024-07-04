package uk.gov.justice.digital.hmpps.integrations.delius.management

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.ZonedDateTime

@Entity
@Table(name = "management_tier")
@EntityListeners(AuditingEntityListener::class)
data class ManagementTier(
    @EmbeddedId
    val id: ManagementTierId,

    @Column
    val tierChangeReasonId: Long,

    @Column
    val partitionAreaId: Long = 0L,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column
    val rowVersion: Long = 0L,

    @Column(nullable = false, updatable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0
)

@Entity
@Table(name = "management_tier")
@EntityListeners(AuditingEntityListener::class)
data class ManagementTierWithEndDate(
    @EmbeddedId
    val id: ManagementTierId,

    @Column
    val tierChangeReasonId: Long,

    @Column
    val endDate: ZonedDateTime,

    @Column
    val partitionAreaId: Long = 0L,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column
    val rowVersion: Long = 0L,

    @Column(nullable = false, updatable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0
)

@Immutable
@Embeddable
data class ManagementTierId(
    @Column(name = "offender_id")
    val personId: Long,

    @Column
    val tierId: Long,

    @Column
    val dateChanged: ZonedDateTime
) : Serializable
