package uk.gov.justice.digital.hmpps.api.model.sentence

import uk.gov.justice.digital.hmpps.api.model.overview.Order
import uk.gov.justice.digital.hmpps.api.model.overview.Rar

data class Sentence(
    val offenceDetails: OffenceDetails,
    val conviction: Conviction? = null,
    val order: Order? = null,
    val rar: Rar? = null
)