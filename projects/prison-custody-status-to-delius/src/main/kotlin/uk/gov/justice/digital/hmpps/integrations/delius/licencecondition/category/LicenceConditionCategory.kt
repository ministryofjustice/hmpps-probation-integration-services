package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.category

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "r_lic_cond_type_main_cat")
@Where(clause = "soft_deleted = 0")
class LicenceConditionCategory(
    @Id
    @Column(name = "lic_cond_type_main_cat_id", nullable = false)
    val id: Long = 0,

    @Column(nullable = false)
    val code: String,
)
