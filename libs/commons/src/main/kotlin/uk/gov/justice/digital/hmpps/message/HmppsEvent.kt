package uk.gov.justice.digital.hmpps.message

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import java.time.ZonedDateTime

interface IntegrationEvent {
    val eventType: String
}

data class HmppsEvent(
    override val eventType: String,
    val version: Int,
    val detailUrl: String,
    val occurredAt: ZonedDateTime,
    val description: String? = null,
    val additionalInformation: AdditionalInformation = AdditionalInformation(),
    val personReference: PersonReference = PersonReference()
) : IntegrationEvent

data class PersonReference(val identifiers: List<PersonIdentifier> = listOf()) {
    fun findCrn(): String? = identifiers.find { it.type == "CRN" }?.value

    operator fun get(key: String) = identifiers.find { it.type == key }?.value
}

data class PersonIdentifier(val type: String, val value: String)

data class AdditionalInformation(@JsonAnyGetter @JsonAnySetter private val info: MutableMap<String, Any?> = mutableMapOf()) {
    operator fun get(key: String): Any? = info[key]
    operator fun set(key: String, value: Any) {
        info[key] = value
    }
}
