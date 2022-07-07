package uk.gov.justice.digital.hmpps.listener

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.jms.JmsProperties
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonOffenderEvent
import javax.jms.Message
import javax.jms.Session
import javax.jms.TextMessage

@Component
class CaseNoteConverter(
    private val objectMapper: ObjectMapper,
    private val jmsProperties: JmsProperties
) : MessageConverter {
    override fun toMessage(caseNoteMessage: Any, session: Session): Message {
        val message = session.createTextMessage()
        message.text = when (caseNoteMessage) {
            is String -> caseNoteMessage
            is PrisonOffenderEvent -> objectMapper.writeValueAsString(PrisonOffenderEventMessage(objectMapper.writeValueAsString(caseNoteMessage)))
            is PrisonOffenderEventMessage -> objectMapper.writeValueAsString(caseNoteMessage)
            else -> throw IllegalArgumentException("Unexpected message type")
        }

        // We should move this somewhere and make it conditional, as it's only required when using ActiveMQ.
        // Amazon SQS supports the JMS 2.0 deliveryDelay property, whereas ActiveMQ does not. See https://github.com/apache/activemq/pull/729.
        // (note: schedulerSupport must be enabled on the ActiveMQ broker)
        message.setLongProperty("AMQ_SCHEDULED_DELAY", jmsProperties.template.deliveryDelay.toMillis())

        return message
    }

    override fun fromMessage(message: Message): PrisonOffenderEvent {
        if (message is TextMessage) {
            return objectMapper.readValue(
                objectMapper.readValue(message.text, PrisonOffenderEventMessage::class.java).message,
                PrisonOffenderEvent::class.java
            )
        }
        throw IllegalArgumentException("Unable to convert $message to a PrisonOffenderEvent")
    }
}

data class PrisonOffenderEventMessage(@JsonProperty("Message") val message: String)
