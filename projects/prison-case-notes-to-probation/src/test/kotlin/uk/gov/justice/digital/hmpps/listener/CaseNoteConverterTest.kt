package uk.gov.justice.digital.hmpps.listener

import com.amazon.sqs.javamessaging.message.SQSObjectMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import javax.jms.Session

class CaseNoteConverterTest {
    @Test
    fun `test when message cannot be converted to a case note`() {
        val objectMapper = ObjectMapper()
        val caseNoteConverter = CaseNoteConverter(objectMapper)
        val message = SQSObjectMessage("This is not a text message")
        assertThrows<IllegalArgumentException> { caseNoteConverter.fromMessage(message) }
    }

    @Test
    fun `test when message cannot be converted from a case note`() {
        val objectMapper = ObjectMapper()
        val caseNoteConverter = CaseNoteConverter(objectMapper)
        val invalid = 123L
        assertThrows<IllegalArgumentException> { caseNoteConverter.toMessage(invalid, mock(Session::class.java)) }
    }
}
