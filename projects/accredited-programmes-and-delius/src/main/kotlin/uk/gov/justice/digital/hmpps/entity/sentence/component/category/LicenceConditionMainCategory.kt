package uk.gov.justice.digital.hmpps.entity.sentence.component.category

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.model.CodedValue

@Entity
@Immutable
@Table(name = "r_lic_cond_type_main_cat")
class LicenceConditionMainCategory(
    @Id
    @Column(name = "lic_cond_type_main_cat_id")
    val id: Long,
    val code: String,
    val description: String,
) {
    fun toCodedValue() = CodedValue(code, description)
}