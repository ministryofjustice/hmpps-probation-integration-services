package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
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
        NOT_KNOWN("NOT_KNOWN", "N"),
        NOT_SPECIFIED("NOT_SPECIFIED", "N")
    }

    enum class StandardRefDataCode(val code: String) {
        INITIAL_ALLOCATION("IN1"),
        TRIAL_ADJOURNMENT_APPEARANCE("T"),
        ADDRESS_MAIN_STATUS("M"),
        AWAITING_ASSESSMENT("A16"),
        REMANDED_IN_CUSTODY_STATUS("Y"),
        REMANDED_IN_CUSTODY_OUTCOME("112"),
        GUILTY("G"),
        NOT_GUILTY("N"),
        NOT_KNOWN_PLEA("Q")
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
    ORDER_ALLOCATION_REASON("ORDER ALLOCATION REASON"),
    COURT_APPEARANCE_TYPE("COURT APPEARANCE TYPE"),
    ADDRESS_STATUS("ADDRESS STATUS"),
    ADDRESS_TYPE("ADDRESS TYPE"),
    REMAND_STATUS("REMAND STATUS"),
    PLEA("PLEA"),
    COURT_APPEARANCE_OUTCOME("COURT APPEARANCE OUTCOME"),
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

    @Column(columnDefinition = "char(6)", nullable = false)
    val code: String,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @Column(name = "court_name", length = 80)
    val name: String,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @Column(name = "court_ou_code")
    val ouCode: String?
)

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.code = :datasetCode and rd.code = :code
    """
    )
    fun findByCodeAndDatasetCode(code: String, datasetCode: DatasetCode): ReferenceData?
}

fun ReferenceDataRepository.initialOmAllocationReason() =
    findByCodeAndDatasetCode(
        ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code,
        DatasetCode.OM_ALLOCATION_REASON
    )
        ?: throw NotFoundException(
            "Allocation Reason",
            "code",
            ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code
        )

fun ReferenceDataRepository.initialOrderAllocationReason() =
    findByCodeAndDatasetCode(
        ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code,
        DatasetCode.ORDER_ALLOCATION_REASON
    )
        ?: throw NotFoundException(
            "Allocation Reason",
            "code",
            ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code
        )

fun ReferenceDataRepository.trialAdjournmentAppearanceType() =
    findByCodeAndDatasetCode(
        ReferenceData.StandardRefDataCode.TRIAL_ADJOURNMENT_APPEARANCE.code,
        DatasetCode.COURT_APPEARANCE_TYPE
    )
        ?: throw NotFoundException(
            "Court Appearance Type",
            "code",
            ReferenceData.StandardRefDataCode.TRIAL_ADJOURNMENT_APPEARANCE.code
        )

fun ReferenceDataRepository.remandedInCustodyOutcome() =
    findByCodeAndDatasetCode(
        ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_OUTCOME.code,
        DatasetCode.COURT_APPEARANCE_OUTCOME
    )
        ?: throw NotFoundException(
            "Court Appearance Outcome",
            "code",
            ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_OUTCOME.code
        )

fun ReferenceDataRepository.remandedInCustodyStatus() =
    findByCodeAndDatasetCode(
        ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_STATUS.code,
        DatasetCode.REMAND_STATUS
    )
        ?: throw NotFoundException(
            "Remand Status",
            "code",
            ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_STATUS.code
        )

fun ReferenceDataRepository.mainAddressStatus() =
    findByCodeAndDatasetCode(ReferenceData.StandardRefDataCode.ADDRESS_MAIN_STATUS.code, DatasetCode.ADDRESS_STATUS)
        ?: throw NotFoundException("Address Status", "code", ReferenceData.StandardRefDataCode.ADDRESS_MAIN_STATUS.code)

fun ReferenceDataRepository.awaitingAssessmentAddressType() =
    findByCodeAndDatasetCode(ReferenceData.StandardRefDataCode.AWAITING_ASSESSMENT.code, DatasetCode.ADDRESS_TYPE)
        ?: throw NotFoundException("Address Type", "code", ReferenceData.StandardRefDataCode.AWAITING_ASSESSMENT.code)

interface CourtRepository : JpaRepository<Court, Long> {
    fun findByOuCode(ouCode: String): Court?
}

fun CourtRepository.getByOuCode(ouCode: String) =
    findByOuCode(ouCode) ?: throw NotFoundException("Court", "ouCode", ouCode)
