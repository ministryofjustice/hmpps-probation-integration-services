package uk.gov.justice.digital.hmpps.api.model.sentence

import java.time.LocalDate

data class Conviction(
    val sentencingCourt: String?,
    val responsibleCourt: String?,
    val convictionDate: LocalDate?,
    val additionalSentences: List<AdditionalSentence>
)
