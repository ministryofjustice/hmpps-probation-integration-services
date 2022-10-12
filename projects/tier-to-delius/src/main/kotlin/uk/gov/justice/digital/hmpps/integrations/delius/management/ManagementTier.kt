package uk.gov.justice.digital.hmpps.integrations.delius.management

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.Immutable
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.EntityListeners

@Entity
@EntityListeners(AuditingEntityListener::class)
data class ManagementTier(
    @EmbeddedId
    var id: ManagementTierId,

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
    var lastUpdatedUserId: Long = 0,
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
