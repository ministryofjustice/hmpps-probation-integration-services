package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.controller.model.CodeAndDescription

@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id", nullable = false)
    val id: Long,

    @Column(name = "code_value", length = 100, nullable = false)
    val code: String,

    @Column(name = "code_description", length = 500, nullable = false)
    val description: String,
) {
    fun toCodeAndDescription(): CodeAndDescription {
        return CodeAndDescription(
            this.code,
            this.description,
        )
    }
}
