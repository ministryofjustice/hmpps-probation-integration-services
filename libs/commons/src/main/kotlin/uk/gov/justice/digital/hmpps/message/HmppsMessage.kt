package uk.gov.justice.digital.hmpps.message

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Component
import javax.jms.Message
import javax.jms.Session
import javax.jms.TextMessage
import kotlin.reflect.KClass

data class HmppsMessage(
    @JsonProperty("Message") val message: String,
    @JsonProperty("MessageAttributes") val metadata: Metadata = Metadata()
)

data class Metadata(@JsonAnyGetter @JsonAnySetter private val attributes: MutableMap<String, MessageAttribute> = mutableMapOf()) {
    constructor(eventType: String) : this(mutableMapOf("eventType" to MessageAttribute("String", eventType)))

    operator fun get(key: String): MessageAttribute? = attributes[key]
    operator fun set(key: String, value: MessageAttribute) {
        attributes[key] = value
    }
}

data class MessageAttribute(@JsonProperty("Type") val type: String, @JsonProperty("Value") val value: String)

abstract class HmppsEventConverter<T : IntegrationEvent>(private val om: ObjectMapper) : MessageConverter {

    protected abstract fun getEventClass(message: HmppsMessage): KClass<T>

    override fun toMessage(hmppsEvent: Any, session: Session): Message {
        val message = session.createTextMessage()
        if (hmppsEvent is IntegrationEvent) {
            message.text = om.writeValueAsString(
                HmppsMessage(om.writeValueAsString(hmppsEvent), Metadata(hmppsEvent.eventType))
            )
        } else {
            throw IllegalArgumentException("Unexpected event type passed to HmppsEventConverter: ${hmppsEvent::class}")
        }
        return message
    }

    override fun fromMessage(message: Message): T {
        if (message is TextMessage) {
            val hmppsMessage: HmppsMessage = om.readValue(message.text)
            return om.readValue(hmppsMessage.message, getEventClass(hmppsMessage).java)
        }
        throw IllegalArgumentException("Unable to convert $message to a Hmpps Event")
    }
}

@Component
class SimpleHmppsEventConverter(om: ObjectMapper) : HmppsEventConverter<HmppsEvent>(om) {
    override fun getEventClass(message: HmppsMessage) = HmppsEvent::class
}
