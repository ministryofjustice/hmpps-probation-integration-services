package uk.gov.justice.digital.hmpps.entity.sentence.licencecondition

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "lic_condition")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class LicenceCondition(
    @Id
    @Column(name = "lic_condition_id")
    val id: Long,
    val startDate: LocalDate,
    val commencementDate: LocalDate?,
    val expectedEndDate: LocalDate?,
    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,
    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: LicenceConditionMainCategory,
    @ManyToOne
    @JoinColumn(name = "lic_cond_type_sub_cat_id")
    val subCategory: ReferenceData?,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val activeFlag: Boolean = true,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)