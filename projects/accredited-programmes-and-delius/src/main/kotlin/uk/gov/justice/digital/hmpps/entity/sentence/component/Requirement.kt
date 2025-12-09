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
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.RequirementMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.component.manager.RequirementManager
import uk.gov.justice.digital.hmpps.entity.sentence.component.transfer.RequirementTransfer
import java.time.ZonedDateTime

@Entity
@Table(name = "rqmnt")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
@EntityListeners(AuditingEntityListener::class)
class Requirement(
    @Id
    @Column(name = "rqmnt_id", nullable = false)
    override val id: Long,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory?,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val subCategory: ReferenceData?,

    @Column(name = "start_date")
    override val startDate: ZonedDateTime,

    @Column(name = "commencement_date")
    override val commencementDate: ZonedDateTime? = null,

    @Column(name = "termination_date")
    override var terminationDate: ZonedDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "rqmnt_termination_reason_id")
    override var terminationReason: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    override val disposal: Disposal,

    @OneToOne(mappedBy = "requirement")
    @SQLRestriction("active_flag = 1")
    override val manager: RequirementManager? = null,

    @OneToMany(mappedBy = "requirement")
    override val pendingTransfers: List<RequirementTransfer> = listOf(),

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    override var pendingTransfer: Boolean = false,

    @Lob
    @Column(name = "rqmnt_notes")
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
        @Transient get() = ReferenceData.REQUIREMENT_COMPLETED
    override val transferRejectionReason: ReferenceData.KnownValue
        @Transient get() = ReferenceData.REQUIREMENT_TRANSFER_REJECTION_REASON
}