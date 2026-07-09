package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter

@Entity
@Table(name = "rqmnt")
@Immutable
class Requirement(
    @Id
    @Column(name = "rqmnt_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val requirementMainCategory: RequirementMainCategory?,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_sub_category_id")
    val requirementSubCategory: RequirementSubCategory?,

    @Column(name = "length")
    val length: Long? = null,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

@Immutable
@Entity
@Table(name = "r_rqmnt_type_main_category")
class RequirementMainCategory(
    @Id
    @Column(name = "rqmnt_type_main_category_id")
    val id: Long,

    val code: String,

    val description: String,
)

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class RequirementSubCategory(
    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,

    @Column(name = "code_value")
    val codeValue: String,

    @Column(name = "code_description")
    val codeDescription: String,

    @Column(name = "reference_data_master_id", nullable = false)
    val datasetId: Long,

    @Column(name = "selectable")
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true
)