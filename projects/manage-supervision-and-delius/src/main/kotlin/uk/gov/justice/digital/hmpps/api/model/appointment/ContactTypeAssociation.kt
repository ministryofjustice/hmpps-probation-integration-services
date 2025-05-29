package uk.gov.justice.digital.hmpps.api.model.appointment

import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import uk.gov.justice.digital.hmpps.api.model.sentence.MinimalSentence

data class ContactTypeAssociation(
    val personSummary: PersonSummary,
    val contactTypeCode: String,
    val associatedWithPerson: Boolean,
    val personNsis: List<MinimalNsi> = emptyList(),
    val sentences: List<MinimalSentence> = emptyList(),
)

data class MinimalNsi(
    val id: Long,
    val description: String
)