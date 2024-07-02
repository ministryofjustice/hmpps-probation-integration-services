package uk.gov.justice.digital.hmpps.api.model.conviction
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime

data class Conviction(
    val convictionId: Long,
    val index: String,
    val active: Boolean,
    val inBreach: Boolean,
    val failureToComplyCount: Long,
    val breachEnd: LocalDate?,
    val awaitingPsr: Boolean = false,
    val convictionDate: LocalDate?,
    val referralDate: LocalDate,
    val offences: List<Offence>,
    val sentence: Sentence?,
    val latestCourtAppearanceOutcome: KeyValue?,
    val custody: Custody? = null,
    val responsibleCourt: Court?,
    val courtAppearance: CourtAppearanceBasic?,
    val orderManagers: List<OrderManager>?
)

data class Offence(
    val offenceId: String,
    val mainOffence: Boolean,
    val detail: OffenceDetail,
    val offenceDate: LocalDate?,
    val offenceCount: Long?,
    val tics: Long?,
    val verdict: String?,
    val offenderId: Long,
    val createdDatetime: ZonedDateTime,
    val lastUpdatedDatetime: ZonedDateTime,
)

data class OffenceDetail(
    val code: String,
    val description: String,
    val abbreviation: String?,
    val mainCategoryCode: String,
    val mainCategoryDescription: String,
    val mainCategoryAbbreviation: String,
    val ogrsOffenceCategory: String,
    val subCategoryCode: String,
    val subCategoryDescription: String,
    val form20Code: String?,
    val subCategoryAbbreviation: String?,
    val cjitCode: String?
)

data class Sentence(
    val sentenceId: Long,
    val description: String,
    val originalLength: Long?,
    val originalLengthUnits: String?,
    val secondLength: Long?,
    val secondLengthUnits: String?,
    val defaultLength: Long?,
    val effectiveLength: Long?,
    val lengthInDays: Long?,
    val expectedSentenceEndDate: LocalDate?,
    val unpaidWork: UnpaidWork?,
    val startDate: LocalDate,
    val terminationDate: LocalDate? = null,
    val terminationReason: String? = null,
    val sentenceType: KeyValue,
    val additionalSentences: List<AdditionalSentence>,
    val failureToComplyLimit: Long?,
    val cja2003Order: Boolean,
    val legacyOrder: Boolean,
)

data class UnpaidWork(
    val minutesOrdered: Long,
    val minutesCompleted: Long,
    val appointments: Appointments,
    val status: String
)

data class Appointments(
    val total: Long,
    val attended: Long,
    val acceptableAbsences: Long,
    val unacceptableAbsences: Long,
    val noOutcomeRecorded: Long,
)

data class AdditionalSentence(
    val additionalSentenceId: Long,
    val type: KeyValue,
    val amount: BigDecimal?,
    val length: Long?,
    val notes: String?,
)

data class Custody(
    val bookingNumber: String?,
    val institution: Institution,
    val keyDates: CustodyRelatedKeyDates,
    val status: KeyValue,
    val sentenceStartDate: LocalDate
)

data class Institution(
    val institutionId: Long,
    val isEstablishment: Boolean,
    val code: String,
    val description: String,
    val institutionName: String?,
    val establishmentType: KeyValue?,
    val isPrivate: Boolean?,
    val nomsPrisonInstitutionCode: String?
)

data class CustodyRelatedKeyDates(
    val conditionalReleaseDate: LocalDate?,
    val licenceExpiryDate: LocalDate?,
    val hdcEligibilityDate: LocalDate?,
    val paroleEligibilityDate: LocalDate?,
    val sentenceExpiryDate: LocalDate?,
    val expectedReleaseDate: LocalDate?,
    val postSentenceSupervisionEndDate: LocalDate?,
    val expectedPrisonOffenderManagerHandoverStartDate: LocalDate?,
    val expectedPrisonOffenderManagerHandoverDate: LocalDate?,
)

data class Court(
    val courtId: Long,
    val code: String,
    val selectable: Boolean,
    val courtName: String?,
    val telephoneNumber: String?,
    val fax: String?,
    val buildingName: String?,
    val street: String?,
    val locality: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val country: String?,
    val courtTypeId: Long,
    val createdDatetime: ZonedDateTime,
    val lastUpdatedDatetime: ZonedDateTime,
    val probationAreaId: Long,
    val secureEmailAddress: String?,
    val probationArea: KeyValue,
    val courtType: KeyValue
)

data class CourtAppearanceBasic(
    val courtAppearanceId: Long,
    val appearanceDate: ZonedDateTime,
    val courtCode: String,
    val courtName: String?,
    val appearanceType: KeyValue,
    val crn: String
)

data class OrderManager(
    val probationAreaId: Long,
    val teamId: Long?,
    val officerId: Long,
    val name: String?,
    val staffCode: String?,
    val dateStartOfAllocation: ZonedDateTime,
    val dateEndOfAllocation: ZonedDateTime?,
    val gradeCode: String?,
    val teamCode: String?,
    val probationAreaCode: String
)

