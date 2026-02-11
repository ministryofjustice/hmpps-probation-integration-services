package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class OffenceDetails(
    val mainOffence: CodedDescription,
    val additionalOffences: List<CodedDescription>,
    val sentencingCourt: String,
    val sentenceDate: LocalDate,
    val sentenceImposed: CodedDescription,
    val requirementsImposed: List<Requirement>,
    val sentence: Sentence
)

data class Requirement(
    val id: Long,
    val startDate: LocalDate,
    val requirementTypeMainCategoryDescription: String,
    val requirementLength: Int,
    val requirementLengthUnits: String,
    val requirementTypeSubCategoryDescription: String,
    val secondaryRequirementLength: Int?,
    val secondaryRequirementLengthUnits: String?
)

data class Sentence(
    val length: Int,
    val lengthUnits: String,
    val type: String,
    val secondLength: Int?,
    val secondLengthUnits: String?
)
