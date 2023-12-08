package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

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
    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,
    @Column(name = "reference_data_master_id")
    val datasetId: Long,
)

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class Dataset(
    @Column(name = "code_set_name")
    val name: String,
    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,
) {
    enum class Code(val value: String) {
        ADDRESS_STATUS("ADDRESS STATUS"),
        DISABILITY("DISABILITY TYPE"),
        ETHNICITY("ETHNICITY"),
        GENDER("GENDER"),
        LANGUAGE("LANGUAGE"),
        NSI_OUTCOME("NSI OUTCOME"),
        RELIGION("RELIGION/FAITH"),
    }
}

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select oc from ReferenceData oc
        join Dataset ds on oc.datasetId = ds.id
        where oc.code = :code
        and ds.name = :datasetName
    """,
    )
    fun findByCode(
        code: String,
        datasetName: String,
    ): ReferenceData?
}

fun ReferenceDataRepository.nsiOutcome(code: String) =
    findByCode(code, Dataset.Code.NSI_OUTCOME.value) ?: throw NotFoundException("NsiOutcome", "code", code)
