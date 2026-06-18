package uk.gov.justice.digital.hmpps.model

import java.math.BigDecimal
import java.time.LocalDate

data class OffenceDetails(
    val mainOffence: CodeAndDescription,
    val additionalOffences: List<CodeAndDescription>,
    val sentencingCourt: String,
    val sentenceDate: LocalDate,
    val sentenceImposed: CodeAndDescription,
    val suspendedCustodyLength: SuspendedCustodyLength?,
    val requirementsImposed: List<Requirement>,
    val sentence: Sentence,
    val additionalSentences: List<AdditionalSentence>,
)

data class SuspendedCustodyLength(
    val length: Int?,
    val units: String?,
)

data class Requirement(
    val id: Long,
    val startDate: LocalDate,
    val mainCategory: String?,
    val subCategory: String?,
    val length: Int?,
    val lengthUnit: String?,
    val secondaryLength: Int?,
    val secondaryLengthUnit: String?,
)

data class Sentence(
    val length: Int?,
    val lengthUnits: String?,
    val type: String?,
    val secondLength: Int?,
    val secondLengthUnits: String?
)

data class AdditionalSentence(
    val length: Long?,
    val amount: BigDecimal?,
    val notes: String?,
    val type: CodeAndDescription?,
    val units: CodeAndDescription?,
)
