package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.model.CodedValue

@Immutable
@Entity
@Table(name = "r_rqmnt_type_main_category")
class RequirementMainCategory(
    @Id
    @Column(name = "rqmnt_type_main_category_id", nullable = false)
    val id: Long,
    val code: String,
    val description: String,
) {
    fun toCodedValue() = CodedValue(code, description)
}