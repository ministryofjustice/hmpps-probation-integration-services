package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateUpdateService
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {

    @Mock
    lateinit var converter: KeyDateChangedEventConverter

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var cduService: CustodyDateUpdateService

    lateinit var handler: Handler

    @Test
    fun `handles unexpected event type`() {
        handler = Handler(converter, cduService, telemetryService)
        val exception = assertThrows<IllegalArgumentException> {
            handler.handle(
                Notification(
                    message = MessageGenerator.SENTENCE_CHANGED,
                    attributes = MessageAttributes(eventType = "UNKNOWN")
                )
            )
        }
        assertThat(exception.message, equalTo("Unexpected offender event type: UNKNOWN"))
    }

    @Test
    fun `throws NotFoundException`() {
        handler = Handler(converter, cduService, telemetryService)

        val event = Notification(
            message = MessageGenerator.SENTENCE_CHANGED,
            attributes = MessageAttributes(eventType = "SENTENCE_CHANGED")
        )
        whenever(cduService.findNomsByCrn(event.message.crn)).thenThrow(
            HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "Person with nomsid not found"
            )
        )
        val exception = assertThrows<HttpClientErrorException> {
            handler.handle(Notification(event.message, event.attributes, event.id))
        }
        assertThat(exception.message, containsString("Person with nomsid not found"))
    }
}
