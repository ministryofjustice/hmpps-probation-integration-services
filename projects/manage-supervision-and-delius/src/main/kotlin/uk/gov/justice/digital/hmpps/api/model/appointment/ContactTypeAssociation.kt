package uk.gov.justice.digital.hmpps.api.model.appointment

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.sentence.AssociationSummary

data class ContactTypeAssociation (
    val personSummary: PersonSummary,
    val contactTypeCode: String,
    val associatedWithPerson: Boolean,
    val events: List<AssociationSummary> = emptyList(),
    val licenceConditions: List<AssociationSummary> = emptyList()
)