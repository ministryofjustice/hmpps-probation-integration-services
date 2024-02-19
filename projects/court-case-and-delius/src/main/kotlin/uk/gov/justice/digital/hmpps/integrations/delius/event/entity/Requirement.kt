package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Disposal
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "rqmnt")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Requirement(

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory?,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val subCategory: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "ad_rqmnt_type_main_category_id")
    val adMainCategory: AdRequirementMainCategory?,

    @ManyToOne
    @JoinColumn(name = "ad_rqmnt_type_sub_category_id")
    val adSubCategory: ReferenceData?,

    @Column(name = "commencement_date")
    val commencementDate: LocalDate? = null,

    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    @Column(name = "termination_date")
    val terminationDate: LocalDate? = null,

    @ManyToOne
    @JoinColumn(name = "rqmnt_termination_reason_id")
    val terminationReason: ReferenceData? = null,

    @Column(name = "expected_start_date")
    val expectedStartDate: LocalDate? = null,

    @Column(name = "expected_end_date")
    val expectedEndDate: LocalDate? = null,

    @Column(name = "length")
    val length: Long? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "rqmnt_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_rqmnt_type_main_category")
class RequirementMainCategory(
    val code: String,
    val description: String,
    @Id
    @Column(name = "rqmnt_type_main_category_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_ad_rqmnt_type_main_category")
class AdRequirementMainCategory(
    val code: String,
    val description: String,
    @Id
    @Column(name = "ad_rqmnt_type_main_category_id")
    val id: Long
)

interface RequirementRepository : JpaRepository<Requirement, Long> {
    fun getAllByDisposal(disposal: Disposal): List<Requirement>
}
