package uk.gov.justice.digital.hmpps.integrations.common.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

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
    ADDRESS_STATUS("ADDRESS STATUS"),
    GENDER("GENDER"),
    ETHNICITY("ETHNICITY"),
    DISABILITY("DISABILITY TYPE"),
    DISABILITY_CONDITION("DISABILITY CONDITION"),
    LANGUAGE("LANGUAGE"),
    REGISTER_LEVEL("REGISTER LEVEL"),
    REGISTER_CATEGORY("REGISTER CATEGORY"),
    DISABILITY_PROVISION("DISABILITY PROVISION"),
    DISABILITY_PROVISION_CATEGORY("DISABILITY PROVISION CATEGORY"),
    RELATIONSHIP("RELATIONSHIP"),
    GENDER_IDENTITY("GENDER IDENTITY"),
    PERSONAL_CONTACT_RELATIONSHIP_TYPE("PERSONAL CONTACT - RELATIONSHIP TYPE"),
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
