package uk.gov.justice.digital.hmpps.message

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import java.time.ZonedDateTime

interface HmppsEvent {
    val eventType: String
}

data class SimpleHmppsEvent(
    override val eventType: String,
    val version: Int,
    val detailUrl: String,
    val occurredAt: ZonedDateTime,
    val description: String? = null,
    val additionalInformation: AdditionalInformation = AdditionalInformation(),
    val personReference: PersonReference = PersonReference()
) : HmppsEvent

data class PersonReference(val identifiers: List<PersonIdentifier> = listOf()) {
    fun findCrn(): String? = identifiers.find { it.type == "CRN" }?.value
}

data class PersonIdentifier(val type: String, val value: String)

data class AdditionalInformation(@JsonAnyGetter @JsonAnySetter val info: MutableMap<String, Any?> = mutableMapOf()) {
    operator fun get(key: String): Any? = info[key]
    operator fun set(key: String, value: Any) {
        info[key] = value
    }
}
