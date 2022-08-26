package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition

import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.category.LicenceConditionCategory
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.manager.LicenceConditionManager
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "lic_condition")
@Where(clause = "soft_deleted = 0")
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
    val softDeleted: Boolean = false,
)
