package uk.gov.justice.digital.hmpps.listener

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationEvent
import javax.jms.Message
import javax.jms.Session
import javax.jms.TextMessage

@Component
class AllocationConverter(private val om: ObjectMapper) : MessageConverter {
    override fun toMessage(allocationMessage: Any, session: Session): Message {
        val message = session.createTextMessage()
        message.text = om.writeValueAsString(AllocationMessage(om.writeValueAsString(allocationMessage)))
        return message
    }

    override fun fromMessage(message: Message): AllocationEvent {
        if (message is TextMessage) {
            return om.readValue(
                om.readValue(message.text, AllocationMessage::class.java).message,
                AllocationEvent::class.java
            )
        }
        throw IllegalArgumentException("Unable to convert $message to a AllocationEvent")
    }
}

data class AllocationMessage(@JsonProperty("Message") val message: String)
