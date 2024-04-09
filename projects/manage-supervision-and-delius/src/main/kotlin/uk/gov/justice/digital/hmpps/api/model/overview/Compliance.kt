package uk.gov.justice.digital.hmpps.api.model.overview

data class Compliance(
    val currentBreaches: Int,
    val breachStarted: Boolean,
    val breachesOnCurrentOrderCount: Int,
    val priorBreachesOnCurrentOrderCount: Int,
    val failureToComplyCount: Int,
)