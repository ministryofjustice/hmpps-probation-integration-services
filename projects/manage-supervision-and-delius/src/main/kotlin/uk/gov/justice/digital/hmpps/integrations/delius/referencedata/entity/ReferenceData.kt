package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Immutable
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id", nullable = false)
    val id: Long,

    @Column(name = "code_value", length = 100, nullable = false)
    val code: String,

    @Column(name = "code_description", length = 500, nullable = false)
    val description: String,

    @Column(name = "reference_data_master_id", nullable = false)
    val datasetId: Long = 0,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true
)

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class Dataset(
    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,

    @Column(name = "code_set_name", nullable = false)
    val code: String,
)

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {

    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.code = :datasetCode and rd.code = :code
    """
    )
    fun findByCodeAndDatasetCode(code: String, datasetCode: String): ReferenceData?

    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.code = :datasetCode and rd.selectable = true
    """
    )
    fun findByDatasetCode(datasetCode: String): List<ReferenceData>
}

fun ReferenceDataRepository.getAddressTypeByCode(code: String) =
    findByCodeAndDatasetCode(code, DatasetCode.ADDRESS_TYPE.code) ?: throw InvalidRequestException(
        "address type code",
        code
    )

fun ReferenceDataRepository.getMainAddressType() =
    findByCodeAndDatasetCode(AddressStatus.MAIN.code, DatasetCode.ADDRESS_STATUS.code) ?: throw NotFoundException(
        "ReferenceData",
        DatasetCode.ADDRESS_STATUS.code,
        AddressStatus.MAIN.code
    )

fun ReferenceDataRepository.getPreviousAddressType() =
    findByCodeAndDatasetCode(AddressStatus.PREVIOUS.code, DatasetCode.ADDRESS_STATUS.code) ?: throw NotFoundException(
        "ReferenceData",
        DatasetCode.ADDRESS_STATUS.code,
        AddressStatus.PREVIOUS.code
    )

enum class DatasetCode(val code: String) {
    ADDRESS_STATUS("ADDRESS STATUS"),
    ADDRESS_TYPE("ADDRESS TYPE");
}

enum class AddressStatus(val code: String) {
    MAIN("M"),
    PREVIOUS("P");
}

enum class LengthUnit(val code: String) {
    YEARS("Y"),
    MONTHS("M");
}

