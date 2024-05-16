package uk.gov.justice.digital.hmpps.api.model.conviction

import uk.gov.justice.digital.hmpps.api.model.KeyValue
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class Conviction(
    val convictionId: Long,
    val index: String,
    val active: Boolean,
    val inBreach: Boolean,
    val failureToComplyCount: Long,
    val breachEnd: LocalDate,
    val awaitingPsr: Boolean,
    val convictionDate: LocalDate,
    val referralDate: LocalDate,
    val offences: List<Offence>,
    val sentence: Sentence,

    )

data class Offence(
    val offenceId: String,
    val mainOffence: Boolean,
    val detail: OffenceDetail,
    val offenceDate: LocalDateTime,
    val offenceCount: Long,
    val tics: Long,
    val verdict: String,
    val offenderId: Long,
    val createdDatetime: LocalDateTime,
    val lastUpdatedDatetime: LocalDateTime,
)

data class OffenceDetail(
    val code: String,
    val description: String,
    val abbreviation: String,
    val mainCategoryCode: String,
    val mainCategoryDescription: String,
    val mainCategoryAbbreviation: String,
    val ogrsOffenceCategory: String,
    val subCategoryCode: String,
    val subCategoryDescription: String,
    val form20Code: String,
    val subCategoryAbbreviation: String,
    val cjitCode: String
)

data class Sentence(
    val sentenceId: Long,
    val description: String,
    val originalLength: Long,
    val originalLengthUnits: String,
    val secondLength: Long,
    val secondLengthUnits: String,
    val defaultLength: Long,
    val effectiveLength: Long,
    val lengthInDays: Long,
    val expectedSentenceEndDate: LocalDate,
    val unpaidWork: UnpaidWork,
    val startDate: LocalDate,
    val terminationDate: LocalDate,
    val terminationReason: String,
    val sentenceType: KeyValue,
    val additionalSentences: List<AdditionalSentence>,
    val failureToComplyLimit: Long,
    val cja2003Order: Boolean,
    val legacyOrder: Boolean,
)

data class UnpaidWork(
    val minutesOrdered: Long,
    val minutesCompleted: Long,
    val appointments: Appointments,
    val status: String
)

class Appointments(
    val total: Long,
    val attended: Long,
    val acceptableAbsences: Long,
    val unacceptableAbsences: Long,
    val noOutcomeRecorded: Long,
)

data class AdditionalSentence(
    val additionalSentenceId: Long,
    val type: KeyValue,
    val amount: BigDecimal,
    val length: Long,
    val notes: String,
)