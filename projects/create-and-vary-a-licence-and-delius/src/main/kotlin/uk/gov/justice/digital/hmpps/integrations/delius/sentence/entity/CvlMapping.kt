package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

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

    val cvlModifier: String?,

    @Column(name = "cvl_populate_lic_cond_text")
    @Convert(converter = YesNoConverter::class)
    val populateNotes: Boolean,

    @Id
    @Column(name = "cvl_lic_cond_mapping_id")
    val id: Long
)

interface CvlMappingRepository : JpaRepository<CvlMapping, Long> {
    fun findByCvlCode(code: String): CvlMapping?

    fun findByCvlCodeAndCvlModifier(code: String, modifier: String?): CvlMapping?
}

fun CvlMappingRepository.getByCvlCode(code: String) =
    findByCvlCode(code) ?: throw NotFoundException("CvlMapping", "cvlCode", code)

fun CvlMappingRepository.getByCvlCodeAndModifier(code: String, modifier: String?) =
    findByCvlCodeAndCvlModifier(code, modifier)
        ?: throw NotFoundException("CvlMapping with code $code and modifier $modifier not found")