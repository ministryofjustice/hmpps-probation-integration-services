package uk.gov.justice.digital.hmpps.entity.sentence.component

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.LicenceConditionMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.component.manager.LicenceConditionManager
import uk.gov.justice.digital.hmpps.entity.sentence.component.transfer.LicenceConditionTransfer
import java.time.ZonedDateTime

@Entity
@Table(name = "lic_condition")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
@EntityListeners(AuditingEntityListener::class)
class LicenceCondition(
    @Id
    @Column(name = "lic_condition_id")
    override val id: Long,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: LicenceConditionMainCategory,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_sub_cat_id")
    val subCategory: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    override val disposal: Disposal,

    @Column(name = "start_date")
    override val startDate: ZonedDateTime,

    @Column(name = "commencement_date")
    override val commencementDate: ZonedDateTime? = null,

    @Column(name = "termination_date")
    override var terminationDate: ZonedDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "lic_termination_reason_id")
    override var terminationReason: ReferenceData? = null,

    @OneToOne(mappedBy = "licenceCondition")
    @SQLRestriction("active_flag = 1")
    override val manager: LicenceConditionManager? = null,

    @OneToMany(mappedBy = "licenceCondition")
    override val pendingTransfers: List<LicenceConditionTransfer> = listOf(),

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    override var pendingTransfer: Boolean = false,

    @Lob
    @Column(name = "lic_condition_notes")
    override var notes: String? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,
) : SentenceComponent {
    override val completedReason: ReferenceData.KnownValue
        @Transient get() = ReferenceData.LICENCE_CONDITION_COMPLETED
    override val transferRejectionReason: ReferenceData.KnownValue
        @Transient get() = ReferenceData.LICENCE_CONDITION_TRANSFER_REJECTION_REASON
}