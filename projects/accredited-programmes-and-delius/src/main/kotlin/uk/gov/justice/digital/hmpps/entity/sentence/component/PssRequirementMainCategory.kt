package uk.gov.justice.digital.hmpps.entity.sentence.component

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.model.CodedValue

@Entity
@Immutable
@Table(name = "r_pss_rqmnt_type_main_category")
class PssRequirementMainCategory(
    @Id
    @Column(name = "pss_rqmnt_type_main_cat_id")
    val id: Long,
    val code: String,
    val description: String,
) {
    fun toCodedValue() = CodedValue(code, description)
}