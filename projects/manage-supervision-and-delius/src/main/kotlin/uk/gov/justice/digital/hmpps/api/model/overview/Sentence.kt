package uk.gov.justice.digital.hmpps.api.model.overview

data class Sentence(
    val additionalOffences: List<AdditionalOffence>,
    val mainOffence: MainOffence,
    val order: Order,
    val rar: Rar
)