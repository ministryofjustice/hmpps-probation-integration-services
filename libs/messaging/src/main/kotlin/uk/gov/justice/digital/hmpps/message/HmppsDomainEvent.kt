package uk.gov.justice.digital.hmpps.message

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import java.time.ZonedDateTime

data class HmppsDomainEvent(
    val eventType: String,
    val version: Int,
    val detailUrl: String? = null,
    val occurredAt: ZonedDateTime = ZonedDateTime.now(),
    val description: String? = null,
    @JsonAlias("additionalInformation") private val nullableAdditionalInformation: AdditionalInformation? = AdditionalInformation(),
    val personReference: PersonReference = PersonReference(),
) {
    val additionalInformation = nullableAdditionalInformation ?: AdditionalInformation()
}

data class PersonReference(val identifiers: List<PersonIdentifier> = listOf()) {
    fun findCrn() = get("CRN")

    fun findNomsNumber() = get("NOMS")

    operator fun get(key: String) = identifiers.find { it.type == key }?.value
}

data class PersonIdentifier(val type: String, val value: String)

data class AdditionalInformation(
    @JsonAnyGetter @JsonAnySetter
    private val info: MutableMap<String, Any?> = mutableMapOf(),
) {
    operator fun get(key: String): Any? = info[key]

    operator fun set(
        key: String,
        value: Any,
    ) {
        info[key] = value
    }

    fun containsKey(key: String): Boolean {
        return info.containsKey(key)
    }
}
