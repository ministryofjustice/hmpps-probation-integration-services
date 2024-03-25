package uk.gov.justice.digital.hmpps.api.model.sentence

data class Sentence(
    val offenceDetails: OffenceDetails,
    val conviction: Conviction? = null,
)