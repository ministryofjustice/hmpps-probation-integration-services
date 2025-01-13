package uk.gov.justice.digital.hmpps.integrations.delius.caseview

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManager

@Immutable
@Entity
@Table(name = "rqmnt")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class CaseViewRequirement(
    @Id
    @Column(name = "rqmnt_id", nullable = false)
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: CaseViewDisposal? = null,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: CaseViewRequirementMainCategory,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val subCategory: ReferenceData?,

    val length: Long?,

    @OneToMany
    @JoinColumn(name = "rqmnt_id")
    @SQLRestriction("active_flag = 1")
    val managers: List<RequirementManager>,

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
@Table(name = "r_rqmnt_type_main_category")
class CaseViewRequirementMainCategory(
    @Id
    @Column(name = "rqmnt_type_main_category_id", nullable = false)
    val id: Long,
    val code: String,
    val description: String,
    @ManyToOne
    @JoinColumn(name = "units_id")
    val units: ReferenceData? = null
)

interface CaseViewRequirementRepository : JpaRepository<CaseViewRequirement, Long> {
    @EntityGraph(attributePaths = ["mainCategory.units.dataset", "subCategory.dataset"])
    fun findAllByDisposalEventId(eventId: Long): List<CaseViewRequirement>
}
