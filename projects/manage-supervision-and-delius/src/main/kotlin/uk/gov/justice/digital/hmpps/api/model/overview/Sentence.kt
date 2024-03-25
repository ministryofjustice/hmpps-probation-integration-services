package uk.gov.justice.digital.hmpps.api.model.overview

data class Sentence(
    val additionalOffences: List<Offence>,
    val eventNumber: String,
    val mainOffence: Offence,
    val order: Order? = null,
    val rar: Rar? = null
)