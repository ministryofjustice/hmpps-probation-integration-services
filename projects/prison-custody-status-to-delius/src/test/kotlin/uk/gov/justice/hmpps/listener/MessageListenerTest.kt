package uk.gov.justice.hmpps.listener

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallService
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseService
import uk.gov.justice.digital.hmpps.listener.MessageListener
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class MessageListenerTest {
    @Mock
    lateinit var releaseService: ReleaseService

    @Mock
    lateinit var recallService: RecallService

    @Mock
    lateinit var telemetryService: TelemetryService

    @InjectMocks
    lateinit var messageListener: MessageListener

    private val notification = Notification(
        message = HmppsDomainEvent(
            "prison-offender-events.prisoner.released", 1, "https//detail/url", ZonedDateTime.now(),
            additionalInformation = AdditionalInformation(
                mutableMapOf(
                    "nomsNumber" to "Z0001ZZ",
                    "prisonId" to "ZZZ",
                    "reason" to "Test data",
                    "details" to "Test data",
                )
            )
        ),
        attributes = MessageAttributes("prison-offender-events.prisoner.released")
    )

    @Test
    fun messageIsLoggedToTelemetry() {
        messageListener.receive(notification)
        verify(telemetryService).notificationReceived(notification)
    }

    @Test
    fun releaseMessagesAreHandled() {
        messageListener.receive(notification)
        verify(releaseService).release("Z0001ZZ", "ZZZ", "Test data", notification.message.occurredAt)
    }

    @Test
    fun recallMessagesAreHandled() {
        messageListener.receive(notification.copy(attributes = MessageAttributes("prison-offender-events.prisoner.received")))

        verify(recallService).recall("Z0001ZZ", "ZZZ", "Test data", notification.message.occurredAt)
    }

    @Test
    fun unknownMessagesAreThrown() {
        assertThrows<IllegalArgumentException> {
            messageListener.receive(notification.copy(attributes = MessageAttributes("unknown")))
        }
    }
}
