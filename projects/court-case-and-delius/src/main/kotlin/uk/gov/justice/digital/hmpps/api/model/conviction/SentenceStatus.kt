package uk.gov.justice.digital.hmpps.api.model.conviction

import uk.gov.justice.digital.hmpps.api.model.KeyValue
import java.time.LocalDate

data class SentenceStatus(
    val sentenceId: Long,
    val custodialType: KeyValue,
    val sentence: KeyValue,
    val mainOffence: KeyValue?,
    val sentenceDate: LocalDate?,
    val actualReleaseDate: LocalDate?,
    val licenceExpiryDate: LocalDate?,
    val pssEndDate: LocalDate? = null,
    val length: Long?,
    val lengthUnit: String
)
