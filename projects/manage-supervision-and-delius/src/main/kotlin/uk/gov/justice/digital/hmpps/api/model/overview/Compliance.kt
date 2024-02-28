package uk.gov.justice.digital.hmpps.api.model.overview

data class Compliance(
    val currentBreaches: Int,
    val failureToComplyInLast12Months: Int
)