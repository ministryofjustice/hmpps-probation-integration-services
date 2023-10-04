package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

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
    val datasetId: Long
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
    val code: DatasetCode
)

enum class DatasetCode(val value: String) {
    ADDRESS_STATUS("ADDRESS STATUS"),
    ADDRESS_TYPE("ADDRESS TYPE"),
    AP_DEPARTURE_REASON("AP DEPARTURE REASON"),
    AP_NON_ARRIVAL_REASON("AP NON ARRIVAL REASON"),
    AP_REFERRAL_CATEGORY("AP REFERRAL CATEGORY"),
    AP_REFERRAL_DATE_TYPE("AP REFERRAL DATE TYPE"),
    ETHNICITY("ETHNICITY"),
    GENDER("GENDER"),
    GENDER_IDENTITY("GENDER IDENTITY"),
    HOSTEL_CODE("HOSTEL CODE"),
    NATIONALITY("NATIONALITY"),
    NSI_OUTCOME("NSI OUTCOME"),
    REFERRAL_DECISION("REFERRAL DECISION"),
    REGISTER_CATEGORY("REGISTER CATEGORY"),
    REGISTER_LEVEL("REGISTER LEVEL"),
    RELIGION("RELIGION/FAITH"),
    RISK_OF_HARM("RISK OF HARM"),
    SOURCE_TYPE("SOURCE TYPE"),
    STAFF_GRADE("OFFICER GRADE"),
    YES_NO("YES NO DONT KNOW");

    companion object {
        private val index = DatasetCode.entries.associateBy { it.value }
        fun fromString(value: String): DatasetCode =
            index[value] ?: throw IllegalArgumentException("Invalid DatasetCode")
    }
}

@Converter
class DatasetCodeConverter : AttributeConverter<DatasetCode, String> {
    override fun convertToDatabaseColumn(attribute: DatasetCode): String = attribute.value

    override fun convertToEntityAttribute(dbData: String): DatasetCode = DatasetCode.fromString(dbData)
}
