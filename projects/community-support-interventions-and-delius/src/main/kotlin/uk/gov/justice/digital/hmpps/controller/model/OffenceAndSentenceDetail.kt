package uk.gov.justice.digital.hmpps.controller.model

import java.time.LocalDate

data class OffenceAndSentenceDetail(
    val offence: CodeAndDescription,
    val offenceSubCategory: CodeAndDescription,
    val outcome: CodeAndDescription,
    val sentenceEndDate: LocalDate
)