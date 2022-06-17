package uk.gov.justice.digital.hmpps.listener

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.nomis.CaseNoteMessage
import javax.jms.Message
import javax.jms.Session
import javax.jms.TextMessage

@Component
class CaseNoteConverter(private val om: ObjectMapper) : MessageConverter {
    override fun toMessage(caseNoteMessage: Any, session: Session): Message {
        TODO("Not yet implemented")
    }

    override fun fromMessage(message: Message): CaseNoteMessage {
        if (message is TextMessage) {
            return om.readValue(message.text, CaseNoteMessageWrapper::class.java).message
        }
        throw IllegalArgumentException("Unable to convert $message to a CaseNoteMessage")
    }
}

data class CaseNoteMessageWrapper(@JsonProperty("Message") val message: CaseNoteMessage)