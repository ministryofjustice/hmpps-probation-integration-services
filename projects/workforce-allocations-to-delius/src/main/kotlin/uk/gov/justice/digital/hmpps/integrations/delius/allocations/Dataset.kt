package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.hibernate.annotations.Immutable
import javax.persistence.AttributeConverter
import javax.persistence.Column
import javax.persistence.Converter
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class Dataset(
    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,

    @Column(name = "code_set_name", length = 100, nullable = false)
    val code: DatasetCode,
)

enum class DatasetCode(val value: String) {
    OM_ALLOCATION_REASON("OM ALLOCATION REASON"),
    ORDER_ALLOCATION_REASON("ORDER ALLOCATION REASON"),
    RM_ALLOCATION_REASON("RM ALLOCATION REASON"),
    TRANSFER_STATUS("TRANSFER STATUS");

    companion object {
        private val index = DatasetCode.values().associateBy { it.value }
        fun fromString(value: String): DatasetCode =
            index[value] ?: throw IllegalArgumentException("Invalid DatasetCode")
    }
}

@Converter
class DatasetCodeConverter : AttributeConverter<DatasetCode, String> {
    override fun convertToDatabaseColumn(attribute: DatasetCode): String = attribute.value

    override fun convertToEntityAttribute(dbData: String): DatasetCode = DatasetCode.fromString(dbData)
}
