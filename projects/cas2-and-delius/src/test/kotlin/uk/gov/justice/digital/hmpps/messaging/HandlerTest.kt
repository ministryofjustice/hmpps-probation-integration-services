package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.prepEvent
import uk.gov.justice.digital.hmpps.service.Cas2Service
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {

    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var cas2Service: Cas2Service

    lateinit var handler: Handler

    @Test
    fun `handles unexpected event type`() {
        handler = Handler(true, converter, telemetryService, cas2Service)
        val exception = assertThrows<IllegalArgumentException> {
            handler.handle(Notification(HmppsDomainEvent("unknown", 1), MessageAttributes("unknown")))
        }

        assertThat(exception.message, equalTo("Unexpected event type ('unknown')"))
    }

    @Test
    fun `throws NotFoundException`() {
        handler = Handler(true, converter, telemetryService, cas2Service)
        val event = prepEvent("application-submitted")
        whenever(cas2Service.applicationSubmitted(event.message)).thenThrow(
            HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "DomainEvent not found"
            )
        )
        val exception = assertThrows<HttpClientErrorException> {
            handler.handle(Notification(event.message, event.attributes, event.id))
        }
        assertThat(exception.message, containsString("DomainEvent not found"))
    }

    @Test
    fun `does not throw NotFoundException`() {
        handler = Handler(false, converter, telemetryService, cas2Service)
        val event = prepEvent("application-submitted")
        whenever(cas2Service.applicationSubmitted(event.message)).thenThrow(
            HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "DomainEvent not found"
            )
        )
        assertDoesNotThrow { handler.handle(Notification(event.message, event.attributes, event.id)) }
    }

    @Test
    fun `still throws Bad Request exception`() {
        handler = Handler(false, converter, telemetryService, cas2Service)
        val event = prepEvent("application-submitted")
        whenever(cas2Service.applicationSubmitted(event.message)).thenThrow(
            HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request"
            )
        )
        val exception = assertThrows<HttpClientErrorException> {
            handler.handle(Notification(event.message, event.attributes, event.id))
        }
        assertThat(exception.message, containsString("Bad Request"))
    }
}
