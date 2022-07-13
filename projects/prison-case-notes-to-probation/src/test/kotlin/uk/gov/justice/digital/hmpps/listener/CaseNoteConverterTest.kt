package uk.gov.justice.digital.hmpps.listener

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.activemq.command.ActiveMQObjectMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.springframework.boot.autoconfigure.jms.JmsProperties
import javax.jms.Session

class CaseNoteConverterTest {
    @Test
    fun `test when message cannot be converted to a case note`() {
        val objectMapper = ObjectMapper()
        val caseNoteConverter = CaseNoteConverter(objectMapper, mock(JmsProperties::class.java))
        val message = ActiveMQObjectMessage()
        assertThrows<IllegalArgumentException> { caseNoteConverter.fromMessage(message) }
    }

    @Test
    fun `test when message cannot be converted from a case note`() {
        val objectMapper = ObjectMapper()
        val caseNoteConverter = CaseNoteConverter(objectMapper, mock(JmsProperties::class.java))
        val invalid = 123L
        assertThrows<IllegalArgumentException> { caseNoteConverter.toMessage(invalid, mock(Session::class.java)) }
    }
}
