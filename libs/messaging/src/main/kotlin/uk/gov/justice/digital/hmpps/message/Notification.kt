package uk.gov.justice.digital.hmpps.message

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class Notification<T>(
    @JsonProperty("Message") val message: T,
    @JsonProperty("MessageAttributes") val attributes: MessageAttributes = MessageAttributes(),
    @JsonProperty("MessageId") val id: UUID = UUID.randomUUID(),
) {
    val eventType: String? @JsonIgnore get() = attributes["eventType"]?.value
}

data class MessageAttributes(
    @JsonAnyGetter @JsonAnySetter
    private val attributes: MutableMap<String, MessageAttribute> = mutableMapOf(),
) : MutableMap<String, MessageAttribute> by attributes {
    constructor(eventType: String) : this(mutableMapOf("eventType" to MessageAttribute("String", eventType)))

    override operator fun get(key: String): MessageAttribute? = attributes[key]

    operator fun set(
        key: String,
        value: MessageAttribute,
    ) {
        attributes[key] = value
    }
}

data class MessageAttribute(
    @JsonProperty("Type") val type: String,
    @JsonProperty("Value") val value: String,
)
