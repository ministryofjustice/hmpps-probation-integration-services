package uk.gov.justice.digital.hmpps.listener

import com.amazon.sqs.javamessaging.message.SQSTextMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CaseNoteConverterTest {
    @Test
    fun `test when message cannot be converted to a case note`() {
        val objectMapper = ObjectMapper()
        val caseNoteConverter = CaseNoteConverter(objectMapper)
        val message = SQSTextMessage("This message cannot be parsed")
        assertThrows<IllegalArgumentException> { caseNoteConverter.fromMessage(message) }
    }
}
