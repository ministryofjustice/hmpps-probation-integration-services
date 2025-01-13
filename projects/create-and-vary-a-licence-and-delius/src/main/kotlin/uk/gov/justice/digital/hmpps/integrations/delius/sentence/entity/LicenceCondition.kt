package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "lic_condition")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
@SequenceGenerator(name = "lic_condition_id_seq", sequenceName = "lic_condition_id_seq", allocationSize = 1)
class LicenceCondition(

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "disposal_id")
    val disposalId: Long,

    val startDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: LicenceConditionCategory,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_sub_cat_id")
    val subCategory: ReferenceData,

    @Lob
    @Column(name = "lic_condition_notes")
    val notes: String?,

    @Column(columnDefinition = "varchar2(4000)")
    val cvlText: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val pendingTransfer: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Id
    @Column(name = "lic_condition_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lic_condition_id_seq")
    val id: Long = 0
) {
    val partitionAreaId: Long = 0

    @Column
    @CreatedBy
    var createdByUserId: Long = 0

    @Column
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0

    @Column
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @Column
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
}

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "lic_condition_manager")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
@SequenceGenerator(
    name = "lic_condition_manager_id_seq",
    sequenceName = "lic_condition_manager_id_seq",
    allocationSize = 1
)
class LicenceConditionManager(

    @Column(name = "lic_condition_id")
    val licenceConditionId: Long,

    val allocationDate: ZonedDateTime,

    @Column(name = "probation_area_id")
    val providerId: Long,
    val teamId: Long,
    val staffId: Long,

    @ManyToOne
    @JoinColumn(name = "transfer_reason_id")
    val transferReason: TransferReason,

    @ManyToOne
    @JoinColumn(name = "allocation_reason_id")
    val allocationReason: ReferenceData?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Id
    @Column(name = "lic_condition_manager_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lic_condition_manager_id_seq")
    val id: Long = 0
) {
    val partitionAreaId: Long = 0

    @Column
    @CreatedBy
    var createdByUserId: Long = 0

    @Column
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0

    @Column
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @Column
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
}

interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long> {
    fun findByDisposalId(disposalId: Long): List<LicenceCondition>
}

interface LicenceConditionManagerRepository : JpaRepository<LicenceConditionManager, Long> {
    fun findByLicenceConditionId(licenceConditionId: Long): LicenceConditionManager?
}
