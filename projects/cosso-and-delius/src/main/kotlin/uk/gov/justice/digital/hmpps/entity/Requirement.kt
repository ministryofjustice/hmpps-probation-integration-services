package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Table(name = "rqmnt")
@SQLRestriction("soft_deleted = 0")
class RequirementEntity(
    @Id
    @Column(name = "rqmnt_id")
    val id: Long,
    val disposalId: Long,
    val startDate: LocalDate,
    val length: Int? = null,
    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val requirementType: RequirementType? = null,
    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val requirementSubType: ReferenceData? = null,
    @Column(name = "length_2")
    val length2: Int? = null,
    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,
    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean

)

@Entity
@Table(name = "r_rqmnt_type_main_category")
class RequirementType(
    @Id
    @Column(name = "rqmnt_type_main_category_id")
    val id: Long,
    val code: String,
    val description: String,
    @ManyToOne
    @JoinColumn(name = "units_id")
    val units: ReferenceData? = null,
    @ManyToOne
    @JoinColumn(name = "length_2_units_id")
    val length2Units: ReferenceData? = null,

    )

interface RequirementRepository : JpaRepository<RequirementEntity, Long> {
    fun findAllByDisposalIdIn(disposalIds: List<Long>): List<RequirementEntity>
    fun getByDisposalId(disposalId: Long): List<RequirementEntity>
}
