package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "r_cvl_lic_cond_mapping")
class CreateAndVaryALicenceMapping(
    @Id
    @Column(name = "cvl_lic_cond_mapping_id")
    val id: Long,

    val cvlCode: String,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: LicenceConditionMainCategory,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_sub_cat_id")
    val subCategory: ReferenceData
)
