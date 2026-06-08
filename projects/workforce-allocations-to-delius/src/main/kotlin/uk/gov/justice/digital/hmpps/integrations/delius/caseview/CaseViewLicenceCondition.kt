package uk.gov.justice.digital.hmpps.integrations.delius.caseview

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionManager

@Immutable
@Entity
@Table(name = "lic_condition")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class CaseViewLicenceCondition(
    @Id
    @Column(name = "lic_condition_id", nullable = false)
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: CaseViewDisposal? = null,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: CaseViewLicenceConditionMainCategory,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_sub_cat_id")
    val subCategory: ReferenceData?,

    val length: Long?,

    @OneToMany
    @JoinColumn(name = "lic_condition_id")
    @SQLRestriction("active_flag = 1")
    val managers: List<LicenceConditionManager>,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
) {
    fun currentManager() = managers.first()
}

@Immutable
@Entity
@Table(name = "r_lic_cond_type_main_cat")
class CaseViewLicenceConditionMainCategory(
    @Id
    @Column(name = "lic_cond_type_main_cat_id", nullable = false)
    val id: Long,
    val code: String,
    val description: String,
    @ManyToOne
    @JoinColumn(name = "units_id")
    val units: ReferenceData? = null
)

interface CaseViewLicenceConditionRepository : JpaRepository<CaseViewLicenceCondition, Long> {
    @EntityGraph(attributePaths = ["mainCategory.units.dataset", "subCategory.dataset"])
    fun findAllByDisposalEventId(eventId: Long): List<CaseViewLicenceCondition>
}