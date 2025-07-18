package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class Offences(
    val mainOffence: Offence,
    val additionalOffences: List<Offence>,
)

data class Offence(
    val date: LocalDate,
    val mainCategoryCode: String,
    val mainCategoryDescription: String,
    val subCategoryCode: String,
    val subCategoryDescription: String,
)