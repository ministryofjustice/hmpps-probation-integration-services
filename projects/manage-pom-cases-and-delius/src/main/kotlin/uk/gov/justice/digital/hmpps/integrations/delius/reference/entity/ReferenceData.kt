package uk.gov.justice.digital.hmpps.integrations.delius.reference.entity

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
@Table(name = "r_standard_reference_list")
class ReferenceData(

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @Column(name = "reference_data_master_id")
    val dataSetId: Long,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class ReferenceDataSet(

    @Column(name = "code_set_name")
    val name: String,

    @Id
    @Column(name = "reference_data_master_id")
    val id: Long
) {
    enum class Code(val value: String) {
        KEY_DATE_TYPE("THROUGHCARE DATE TYPE"),
        POM_ALLOCATION_REASON("POM ALLOCATION REASON")
    }
}

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from ReferenceData rd 
        join ReferenceDataSet ds on rd.dataSetId = ds.id
        where ds.name = :dataSetName and rd.code = :code
    """
    )
    fun findByCode(code: String, dataSetName: String): ReferenceData?
}

fun ReferenceDataRepository.keyDateType(code: String) =
    findByCode(code, ReferenceDataSet.Code.KEY_DATE_TYPE.value) ?: throw NotFoundException("KeyDateType", "code", code)

fun ReferenceDataRepository.pomAllocationReason(code: String) =
    findByCode(code, ReferenceDataSet.Code.POM_ALLOCATION_REASON.value)
        ?: throw NotFoundException("PomAllocationReason", "code", code)
