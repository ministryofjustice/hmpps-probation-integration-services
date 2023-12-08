package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class Sentence(
    val description: String,
    val length: Long?,
    val lengthUnits: String?,
    val isCustodial: Boolean,
    val custodialStatusCode: String?,
    val licenceExpiryDate: LocalDate?,
    val sentenceExpiryDate: LocalDate?,
)
