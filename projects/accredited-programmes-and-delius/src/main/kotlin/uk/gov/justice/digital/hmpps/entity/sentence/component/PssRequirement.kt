package uk.gov.justice.digital.hmpps.entity.sentence.component

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.PssRequirementMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.component.category.PssRequirementSubCategory
import uk.gov.justice.digital.hmpps.entity.sentence.custody.Custody

@Entity
@Immutable
@Table(name = "pss_rqmnt")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PssRequirement(
    @Id
    @Column(name = "pss_rqmnt_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "pss_rqmnt_type_main_cat_id")
    val mainCategory: PssRequirementMainCategory,

    @ManyToOne
    @JoinColumn(name = "pss_rqmnt_type_sub_cat_id")
    val subCategory: PssRequirementSubCategory?,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)