package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class Sentence(
    val description: String?,
    val startDate: LocalDate,
    val expectedEndDate: LocalDate?,
    val licenceExpiryDate: LocalDate?,
    val postSentenceSupervisionEndDate: LocalDate?,
    val twoThirdsSupervisionDate: LocalDate?,
    val custodial: Boolean,
    val releaseType: String?,
    val licenceConditions: List<CodedValue>,
    val requirements: List<CodedValue>,
    val postSentenceSupervisionRequirements: List<CodedValue>,
)
