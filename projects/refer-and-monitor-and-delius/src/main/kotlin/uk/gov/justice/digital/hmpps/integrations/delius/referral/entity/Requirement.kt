package uk.gov.justice.digital.hmpps.integrations.delius.referral.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where

@Immutable
@Entity
@Table(name = "rqmnt")
@Where(clause = "soft_deleted = 0 and active_flag = 1")
class Requirement(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory,

    @Column(name = "disposal_id")
    val disposalId: Long,

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
    @Id
    @Column(name = "rqmnt_type_main_category_id")
    val id: Long
) {
    enum class Code(val value: String) {
        REHAB_ACTIVITY_TYPE("F")
    }
}
