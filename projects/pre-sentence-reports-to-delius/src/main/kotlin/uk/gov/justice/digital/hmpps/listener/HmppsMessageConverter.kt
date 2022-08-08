package uk.gov.justice.digital.hmpps.listener

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Component
import javax.jms.Message
import javax.jms.Session
import javax.jms.TextMessage

@Component
class HmppsMessageConverter(private val om: ObjectMapper) : MessageConverter {
    override fun toMessage(hmppsMessage: Any, session: Session): Message {
        val message = session.createTextMessage()
        message.text = om.writeValueAsString(HmppsMessage(om.writeValueAsString(hmppsMessage)))
        return message
    }

    override fun fromMessage(message: Message): HmppsEvent {
        if (message is TextMessage) {
            return om.readValue(
                om.readValue(message.text, HmppsMessage::class.java).message,
                HmppsEvent::class.java
            )
        }
        throw IllegalArgumentException("Unable to convert $message to a HmppsMessage")
    }
}

data class HmppsMessage(@JsonProperty("Message") val message: String)
