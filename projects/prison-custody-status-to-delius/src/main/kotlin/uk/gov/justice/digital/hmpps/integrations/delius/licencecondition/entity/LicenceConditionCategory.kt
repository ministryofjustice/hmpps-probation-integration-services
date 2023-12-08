package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "r_lic_cond_type_main_cat")
class LicenceConditionCategory(
    @Id
    @Column(name = "lic_cond_type_main_cat_id", nullable = false)
    val id: Long = 0,
    @Column(nullable = false)
    val code: String,
) {
    enum class Code(val value: String) {
        ACCREDITED_PROGRAM("LAP"),
    }
}
