package uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity

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
    val code: DatasetCode
)

enum class DatasetCode(val value: String) {
    ADDRESS_STATUS("ADDRESS STATUS"),
    ADDRESS_TYPE("ADDRESS TYPE"),
    AP_REFERRAL_CATEGORY("AP REFERRAL CATEGORY"),
    BULK_ALLOCATION_REASON("BULK ALLOCATION REASON"),
    BULK_TRANSFER_REASON("BULK TRANSFER REASON"),
    COURT_APPEARANCE_TYPE("COURT APPEARANCE TYPE"),
    CUSTODY_STATUS("CUSTODY STATUS"),
    GENDER("GENDER"),
    INSTITUTIONAL_REPORT_TYPE("IREPORTTYPE"),
    INTER_AREA_ORDER_TRANSFER_REASON("INTER AREA ORDER TRANSFER REASON"),
    INTER_AREA_REQUIREMENT_TRANSFER_REASON("INTER AREA REQUIREMENT TRANSFER REASON"),
    INTER_AREA_TRANSFER_REASON("INTER AREA TRANSFER REASON"),
    OFFICER_GRADE("OFFICER GRADE"),
    OM_ALLOCATION_REASON("OM ALLOCATION REASON"),
    ORDER_ALLOCATION_REASON("ORDER ALLOCATION REASON"),
    REGISTER_TYPE_FLAG("REGISTER TYPE FLAG"),
    REQUIREMENT_SUB_CATEGORY("REQUIREMENT SUB CATEGORY"),
    RM_ALLOCATION_REASON("RM ALLOCATION REASON"),
    TITLE("TITLE"),
    TRANSFER_STATUS("TRANSFER STATUS"),
    THROUGHCARE_DATE_TYPE("THROUGHCARE DATE TYPE"),
    UNITS("UNITS");

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
