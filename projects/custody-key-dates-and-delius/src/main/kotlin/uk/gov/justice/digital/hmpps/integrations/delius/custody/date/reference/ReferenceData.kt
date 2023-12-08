package uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference

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

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,
    @Column(name = "code_value")
    val code: String,
    @Column(name = "code_description")
    val description: String,
    @ManyToOne
    @JoinColumn(name = "reference_data_master_id")
    val dataset: Dataset,
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
    CUSTODY_STATUS("THROUGHCARE STATUS"),
    KEY_DATE_TYPE("THROUGHCARE DATE TYPE"),
    ;

    companion object {
        private val index = DatasetCode.values().associateBy { it.value }

        fun fromString(value: String): DatasetCode = index[value] ?: throw IllegalArgumentException("Invalid DatasetCode")
    }
}

@Converter
class DatasetCodeConverter : AttributeConverter<DatasetCode, String> {
    override fun convertToDatabaseColumn(attribute: DatasetCode): String = attribute.value

    override fun convertToEntityAttribute(dbData: String): DatasetCode = DatasetCode.fromString(dbData)
}
