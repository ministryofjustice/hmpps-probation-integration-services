package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "lic_condition")
@SQLRestriction("soft_deleted = 0")
class LicenceCondition(
    @Id
    @Column(name = "lic_condition_id", nullable = false)
    val id: Long = 0,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: LicenceConditionCategory,

    @OneToOne(mappedBy = "licenceCondition")
    val manager: LicenceConditionManager? = null,

    @Column(name = "pending_transfer", columnDefinition = "number")
    var pendingTransfer: Boolean? = null,

    @ManyToOne
    @JoinColumn(name = "lic_termination_reason_id")
    var terminationReason: ReferenceData? = null,

    @Column(name = "termination_date")
    var terminationDate: ZonedDateTime? = null,

    @Column
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long> {
    fun findAllByDisposalIdAndMainCategoryCodeNotAndTerminationReasonIsNull(
        disposalId: Long,
        excludedCategory: String = LicenceConditionCategory.Code.ACCREDITED_PROGRAM.value
    ): List<LicenceCondition>
}
