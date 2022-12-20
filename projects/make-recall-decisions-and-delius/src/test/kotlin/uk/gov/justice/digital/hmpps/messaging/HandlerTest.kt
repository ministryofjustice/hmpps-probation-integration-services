package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.RecommendationStarted
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @Mock
    lateinit var recommendationStarted: RecommendationStarted

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `message is logged to telemetry`() {
        val message = MessageGenerator.RECOMMENDATION_STARTED
        val notification = Notification(message, MessageAttributes(message.eventType))

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
    }

    @Test
    fun `unknown message throws exception`() {
        val message = MessageGenerator.RECOMMENDATION_STARTED.copy("unknown-type")
        val notification = Notification(message, MessageAttributes(message.eventType))

        assertThrows<NotImplementedError> { handler.handle(notification) }
    }
}
