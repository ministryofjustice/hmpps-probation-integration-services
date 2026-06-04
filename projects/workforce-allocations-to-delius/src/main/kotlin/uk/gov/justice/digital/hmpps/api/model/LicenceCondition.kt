package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class LicenceCondition(
    val id: Long,
    val mainCategory: String,
    val subCategory: String?,
    val startDate: LocalDate,
    val commencementDate: LocalDate?,
    val terminationDate: LocalDate?,
    val active: Boolean,
)
