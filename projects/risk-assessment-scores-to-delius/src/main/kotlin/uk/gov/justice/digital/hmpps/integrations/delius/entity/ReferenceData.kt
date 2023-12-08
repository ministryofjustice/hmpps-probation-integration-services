package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        SELECT rd FROM ReferenceData rd
        WHERE rd.dataset.code = :datasetCode
        AND rd.code = :code
    """,
    )
    fun findByDatasetAndCode(
        datasetCode: DatasetCode,
        code: String,
    ): ReferenceData?
}

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id", nullable = false)
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "reference_data_master_id")
    val dataset: Dataset,
    @Column(name = "code_value", length = 100, nullable = false)
    val code: String,
    @Column(name = "code_description", length = 500, nullable = false)
    val description: String,
)

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class Dataset(
    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,
    @Convert(converter = DatasetCodeConverter::class)
    @Column(name = "code_set_name", nullable = false)
    val code: DatasetCode,
)

enum class DatasetCode(val value: String) {
    GENDER("GENDER"),
    TIER_CHANGE_REASON("TIER CHANGE REASON"),
    TIER("TIER"),
    ;

    companion object {
        private val index = DatasetCode.values().associateBy { it.value }

        fun fromString(value: String): DatasetCode =
            index[value] ?: throw IllegalArgumentException("Invalid DatasetCode: $value")
    }
}

@Converter
class DatasetCodeConverter : AttributeConverter<DatasetCode, String> {
    override fun convertToDatabaseColumn(attribute: DatasetCode): String = attribute.value

    override fun convertToEntityAttribute(dbData: String): DatasetCode = DatasetCode.fromString(dbData)
}
