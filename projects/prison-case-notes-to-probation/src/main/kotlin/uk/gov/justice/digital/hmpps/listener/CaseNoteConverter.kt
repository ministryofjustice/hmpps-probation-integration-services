package uk.gov.justice.digital.hmpps.listener

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonOffenderEvent
import javax.jms.Message
import javax.jms.Session
import javax.jms.TextMessage

@Component
class CaseNoteConverter(private val om: ObjectMapper) : MessageConverter {
    override fun toMessage(caseNoteMessage: Any, session: Session): Message {
        val message = session.createTextMessage()
        message.text = when (caseNoteMessage) {
            is String -> caseNoteMessage
            is PrisonOffenderEvent -> om.writeValueAsString(PrisonOffenderEventMessage(om.writeValueAsString(caseNoteMessage)))
            is PrisonOffenderEventMessage -> om.writeValueAsString(caseNoteMessage)
            else -> throw IllegalArgumentException("Unexpected message type")
        }
        // We could move this somewhere and make it conditional, as it's only required when using ActiveMQ.
        // SQS has the "Delay Seconds" property to achieve the same thing - see https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-delay-queues.html
        message.setLongProperty("AMQ_SCHEDULED_DELAY", 60 * 1000)
        return message
    }

    override fun fromMessage(message: Message): PrisonOffenderEvent {
        if (message is TextMessage) {
            return om.readValue(om.readValue(message.text, PrisonOffenderEventMessage::class.java).message, PrisonOffenderEvent::class.java)
        }
        throw IllegalArgumentException("Unable to convert $message to a PrisonOffenderEvent")
    }
}

data class PrisonOffenderEventMessage(@JsonProperty("Message") val message: String)
