package uk.gov.justice.digital.hmpps.api.model.overview

data class OverviewSentence(
    val additionalOffences: List<Offence>,
    val eventNumber: String,
    val mainOffence: Offence,
    val order: Order? = null,
    val rarDescription: String? = null,
)