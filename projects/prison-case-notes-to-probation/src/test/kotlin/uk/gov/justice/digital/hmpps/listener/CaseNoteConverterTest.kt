package uk.gov.justice.digital.hmpps.listener

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.activemq.command.ActiveMQObjectMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CaseNoteConverterTest {
    @Test
    fun `test when message cannot be converted to a case note`() {
        val objectMapper = ObjectMapper()
        val caseNoteConverter = CaseNoteConverter(objectMapper)
        val message = ActiveMQObjectMessage()
        assertThrows<IllegalArgumentException> { caseNoteConverter.fromMessage(message) }
    }
}
