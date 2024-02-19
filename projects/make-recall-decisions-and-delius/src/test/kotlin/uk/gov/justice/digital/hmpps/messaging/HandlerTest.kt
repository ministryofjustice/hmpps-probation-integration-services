package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.integrations.makerecalldecisions.MakeRecallDecisionsClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.service.RecommendationService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @Mock
    lateinit var recommendationService: RecommendationService

    @Mock
    lateinit var makeRecallDecisionsClient: MakeRecallDecisionsClient

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `message is logged to telemetry`() {
        val message = MessageGenerator.DECISION_NOT_TO_RECALL
        val notification = Notification(message, MessageAttributes(message.eventType))

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
    }

    @Test
    fun `unknown message throws exception`() {
        val message = MessageGenerator.DECISION_TO_RECALL.copy("unknown-type")
        val notification = Notification(message, MessageAttributes(message.eventType))

        assertThrows<NotImplementedError> { handler.handle(notification) }
    }

    @Test
    fun `message without crn throws exception`() {
        val message = MessageGenerator.DECISION_NOT_TO_RECALL.copy(personReference = PersonReference())
        val notification = Notification(message, MessageAttributes(message.eventType))

        val exception = assertThrows<IllegalArgumentException> { handler.handle(notification) }
        assertThat(exception.message, equalTo("CRN not found in message"))
    }

    @Test
    fun `decision message without detail url throws exception`() {
        val message = MessageGenerator.DECISION_NOT_TO_RECALL.copy(detailUrl = null)
        val notification = Notification(message, MessageAttributes(message.eventType))

        val exception = assertThrows<IllegalArgumentException> { handler.handle(notification) }
        assertThat(exception.message, equalTo("No detail url provided"))
    }

    @Test
    fun `decision message with blank detail url throws exception`() {
        val message = MessageGenerator.DECISION_NOT_TO_RECALL.copy(detailUrl = "")
        val notification = Notification(message, MessageAttributes(message.eventType))

        val exception = assertThrows<IllegalArgumentException> { handler.handle(notification) }
        assertThat(exception.message, equalTo("No detail url provided"))
    }

    @Test
    fun `calls detail url`() {
        val message = MessageGenerator.DECISION_TO_RECALL
        val notification = Notification(message, MessageAttributes(message.eventType))

        handler.handle(notification)

        verify(makeRecallDecisionsClient).getDetails(URI(message.detailUrl!!))
    }
}
