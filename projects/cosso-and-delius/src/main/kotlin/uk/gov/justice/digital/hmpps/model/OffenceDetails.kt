package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class OffenceDetails(
    val mainOffence: CodeAndDescription,
    val additionalOffences: List<CodeAndDescription>,
    val sentencingCourt: String,
    val sentenceDate: LocalDate,
    val sentenceImposed: CodeAndDescription,
    val requirementsImposed: List<Requirement>,
    val sentence: Sentence
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
