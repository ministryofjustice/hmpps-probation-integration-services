package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "r_lic_cond_type_main_cat")
class LicenceConditionCategory(
    val code: String,

    @Id
    @Column(name = "lic_cond_type_main_cat_id")
    val id: Long
) {
    companion object {
        val STANDARD_CATEGORY_CODE = "SL1"
        val BESPOKE_CATEGORY_CODE = "BESP"
    }
}

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @Column(name = "reference_data_master_id")
    val datasetId: Long,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
) {
    companion object {
        val STANDARD_SUB_CATEGORY_CODE = "SL1"
        val BESPOKE_SUB_CATEGORY_CODE = "NSTT9"
        val INITIAL_ALLOCATION_CODE = "IN1"
    }
}

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class Dataset(

    @Column(name = "code_set_name")
    val code: String,

    @Id
    @Column(name = "reference_data_master_id")
    val id: Long
) {
    companion object {
        val TRANSFER_REASON_CODE = "INTER AREA LICENCE TRANSFER REASON"
        val SUB_CATEGORY_CODE = "LICENCE CONDITION SUB CATEGORY"
    }
}

interface LicenceConditionCategoryRepository : JpaRepository<LicenceConditionCategory, Long> {
    fun findByCode(code: String): LicenceConditionCategory?
}

fun LicenceConditionCategoryRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("LicenceConditionMainCategory", "code", code)

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.code = :datasetCode and rd.code = :code
    """
    )
    fun findByCodeAndDatasetCode(code: String, datasetCode: String): ReferenceData?
}

fun ReferenceDataRepository.getByCodeAndDatasetCode(code: String, datasetCode: String) =
    findByCodeAndDatasetCode(code, datasetCode)
        ?: throw NotFoundException("Reference Data Not Found: $datasetCode => $code")

fun ReferenceDataRepository.getLicenceConditionSubCategory(code: String) =
    getByCodeAndDatasetCode(code, Dataset.SUB_CATEGORY_CODE)

fun ReferenceDataRepository.getLicenceTransferReason(code: String) =
    getByCodeAndDatasetCode(code, Dataset.TRANSFER_REASON_CODE)