package uk.gov.justice.digital.hmpps.message

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Component
import javax.jms.Message
import javax.jms.Session
import javax.jms.TextMessage

data class HmppsMessage(@JsonProperty("Message") val message: String)

@Component
class HmppsEventConverter(private val om: ObjectMapper) : MessageConverter {

    override fun toMessage(hmppsEvent: Any, session: Session): Message {
        val message = session.createTextMessage()
        message.text = om.writeValueAsString(HmppsMessage(om.writeValueAsString(hmppsEvent)))
        return message
    }

    override fun fromMessage(message: Message): HmppsEvent {
        if (message is TextMessage) {
            return om.readValue(
                om.readValue(message.text, HmppsMessage::class.java).message
            )
        }
        throw IllegalArgumentException("Unable to convert $message to a Hmpps Event")
    }
}
