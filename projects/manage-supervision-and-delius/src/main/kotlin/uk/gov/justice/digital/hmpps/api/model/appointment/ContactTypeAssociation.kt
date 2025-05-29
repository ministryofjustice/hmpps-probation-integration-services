package uk.gov.justice.digital.hmpps.api.model.appointment

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.sentence.OrderSummary

data class ContactTypeAssociation (
    val personSummary: PersonSummary,
    val contactTypeCode: String,
    val associatedWithPerson: Boolean,
    val events: List<OrderSummary> = emptyList()
)