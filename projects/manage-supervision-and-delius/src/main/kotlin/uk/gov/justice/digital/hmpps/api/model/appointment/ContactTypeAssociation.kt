package uk.gov.justice.digital.hmpps.api.model.appointment

import uk.gov.justice.digital.hmpps.api.model.PersonSummary

data class ContactTypeAssociation (
    val personSummary: PersonSummary,
    val contactTypeCode: String,
    val associatedWithPerson: Boolean
)