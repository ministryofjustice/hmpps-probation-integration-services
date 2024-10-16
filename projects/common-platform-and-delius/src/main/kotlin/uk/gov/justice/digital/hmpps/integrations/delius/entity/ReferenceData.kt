package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id", nullable = false)
    val id: Long,

    @Column(name = "code_value", length = 100, nullable = false)
    val code: String,

    @Column(name = "reference_data_master_id", nullable = false)
    val datasetId: Long,

    @Column(name = "code_description", length = 500, nullable = false)
    val description: String,
) {
    enum class GenderCode(val commonPlatformValue: String, val deliusValue: String) {
        MALE("MALE", "M"),
        FEMALE("FEMALE", "F"),
        OTHER("OTHER", "O"),
        NOT_KNOWN("NOT KNOWN", "N")
    }

    enum class AllocationCode(val code: String) {
        INITIAL_ALLOCATION("IN1")
    }
}

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
    OM_ALLOCATION_REASON("OM ALLOCATION REASON"),
    GENDER("GENDER");

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

@Entity
@Immutable
@Table(name = "probation_area")
class Provider(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    @Column
    val description: String,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @Column
    val endDate: LocalDate? = null
)

@Entity
@Immutable
@Table(name = "court")
class Court(
    @Id
    @Column(name = "court_id", nullable = false)
    val id: Long,

    @Column(name = "code", length = 6, nullable = false)
    val code: String,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @Column(name = "code_description", length = 80)
    val courtName: String,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: Provider,
) {
    enum class CourtCode(val commonPlatformValue: String, val deliusValue: String) {
        TEST("A00AA00", "UNKNCT")
    }
}
