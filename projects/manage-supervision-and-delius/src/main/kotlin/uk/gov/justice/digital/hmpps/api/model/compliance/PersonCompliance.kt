package uk.gov.justice.digital.hmpps.api.model.compliance

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.overview.PreviousOrders

data class PersonCompliance(
    val personSummary: PersonSummary,
    val currentSentences: List<SentenceCompliance>,
    val previousOrders: PreviousOrders
)
