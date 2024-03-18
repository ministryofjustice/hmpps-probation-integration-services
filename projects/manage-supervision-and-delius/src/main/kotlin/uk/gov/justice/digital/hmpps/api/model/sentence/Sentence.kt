package uk.gov.justice.digital.hmpps.api.model.sentence

data class Sentence(
    val offence: OffenceDetails,
    val conviction: Conviction? = null,

)