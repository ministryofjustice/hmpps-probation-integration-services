package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
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
    val length: Int,
    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val requirementType: RequirementType,
    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val requirementSubType: ReferenceData,
    @Column(name = "length_2")
    val length2: Int?,
    val softDeleted: Int = 0
)

@Entity
@Table(name = "r_rqmnt_type_main_category")
class RequirementType(
    @Id
    @Column(name = "rqmnt_type_id")
    val id: Long,
    val description: String,
    @ManyToOne
    @JoinColumn(name = "units_id")
    val units: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "length_2_units_id")
    val length2Units: ReferenceData,

    )

interface RequirementRepository : JpaRepository<RequirementEntity, Long> {
    fun getByDisposalId(disposalId: Long): List<RequirementEntity>
}