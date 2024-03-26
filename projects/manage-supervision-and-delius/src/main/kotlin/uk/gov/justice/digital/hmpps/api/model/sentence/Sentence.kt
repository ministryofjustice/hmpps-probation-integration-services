package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.overview.Order

data class Sentence(
    val offenceDetails: OffenceDetails,
    val conviction: Conviction? = null,
    val order: Order? = null,
)