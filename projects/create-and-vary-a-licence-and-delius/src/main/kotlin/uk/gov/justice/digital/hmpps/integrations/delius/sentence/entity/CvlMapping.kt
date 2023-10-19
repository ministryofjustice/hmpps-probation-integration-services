package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "r_cvl_lic_cond_mapping")
class CvlMapping(

    val cvlCode: String,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_main_cat_id")
    val mainCategory: LicenceConditionCategory,

    @ManyToOne
    @JoinColumn(name = "lic_cond_type_sub_cat_id")
    val subCategory: ReferenceData,

    @Id
    @Column(name = "cvl_lic_cond_mapping_id")
    val id: Long
) {
    companion object {
        val STANDARD_CATEGORY_CODE = "SL1"
        val STANDARD_SUB_CATEGORY_CODE = "SL1"
        val BESPOKE_CATEGORY_CODE = "BESP"
        val BESPOKE_SUB_CATEGORY_CODE = "NSTT9"
    }
}

interface CvlMappingRepository : JpaRepository<CvlMapping, Long> {
    fun findByCvlCode(code: String): CvlMapping?
}

fun CvlMappingRepository.getByCvlCode(code: String) =
    findByCvlCode(code) ?: throw uk.gov.justice.digital.hmpps.exception.NotFoundException("CvlMapping", "cvlCode", code)
