package uk.gov.justice.digital.hmpps.api.model.overview

data class OverviewSentence(
    val additionalOffences: List<OverviewOffence>,
    val eventNumber: String,
    val mainOffence: OverviewOffence,
    val order: Order? = null,
    val rarDescription: String? = null,
)