package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity(name = "DomainReferenceData")
@Table(name = "r_standard_reference_list")
class ReferenceData(

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @Column(name = "reference_data_master_id")
    val datasetId: Long

)

@Entity
@Table(name = "r_reference_data_master")
class Dataset(

    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,

    @Column(name = "code_set_name")
    val name: String,

    ) {
    enum class Code(val value: String) {
        DOMAIN_EVENT_TYPE("DOMAIN EVENT TYPE")
    }
}

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from DomainReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.name = :datasetCode and rd.code = :code
        """
    )
    fun findByCode(code: String, datasetCode: String): ReferenceData?
}

fun ReferenceDataRepository.getByCode(code: String, datasetCode: String) =
    findByCode(code, datasetCode) ?: throw NotFoundException(datasetCode, "code", code)

fun ReferenceDataRepository.domainEventType(code: String) = getByCode(code, Dataset.Code.DOMAIN_EVENT_TYPE.value)

interface DatasetRepository : JpaRepository<Dataset, Long>
