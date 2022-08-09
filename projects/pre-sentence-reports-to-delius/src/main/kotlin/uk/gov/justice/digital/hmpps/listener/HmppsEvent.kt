package uk.gov.justice.digital.hmpps.listener

import java.time.ZonedDateTime

data class HmppsEvent(
    val description: String,
    val detailUrl: String,
    val occurredAt: ZonedDateTime,
    val additionalInformation: AdditionalInformation,
    val personReference: PersonReference
)

data class AdditionalInformation(val allocationId: String)

data class PersonReference(val identifiers: List<PersonIdentifier>) {
    fun findCrn(): String? = identifiers.find { it.type == "CRN" }?.value
}

data class PersonIdentifier(val type: String, val value: String)
