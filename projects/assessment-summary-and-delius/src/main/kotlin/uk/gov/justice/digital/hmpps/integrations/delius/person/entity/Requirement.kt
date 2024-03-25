package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

@Immutable
@Entity
@Table(name = "rqmnt")
class Requirement(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory?,

    @ManyToOne
    @JoinColumn(name = "ad_rqmnt_type_main_category_id")
    val additionalMainCategory: RequirementAdditionalMainCategory?,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val subCategory: ReferenceData?,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

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
)

@Immutable
@Entity
@Table(name = "r_ad_rqmnt_type_main_category")
class RequirementAdditionalMainCategory(
    val code: String,
    @Id
    @Column(name = "ad_rqmnt_type_main_category_id")
    val id: Long
)