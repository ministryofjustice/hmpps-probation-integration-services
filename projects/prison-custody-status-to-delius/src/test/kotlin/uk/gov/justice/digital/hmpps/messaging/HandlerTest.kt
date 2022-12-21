package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallService
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseService
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var releaseService: ReleaseService

    @Mock
    lateinit var recallService: RecallService

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    private lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @InjectMocks
    lateinit var handler: Handler

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
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
    }

    @Test
    fun releaseMessagesAreHandled() {
        handler.handle(notification)
        verify(releaseService).release("Z0001ZZ", "ZZZ", "Test data", notification.message.occurredAt)
    }

    @Test
    fun recallMessagesAreHandled() {
        whenever(recallService.recall("Z0001ZZ", "ZZZ", "Test data", notification.message.occurredAt)).thenReturn(RecallOutcome.PrisonerRecalled)
        handler.handle(notification.copy(attributes = MessageAttributes("prison-offender-events.prisoner.received")))
        verify(recallService).recall("Z0001ZZ", "ZZZ", "Test data", notification.message.occurredAt)
        verify(telemetryService).trackEvent("PrisonerRecalled", notification.message.telemetryProperties())
    }

    @Test
    fun unknownMessagesAreThrown() {
        assertThrows<IllegalArgumentException> {
            handler.handle(notification.copy(attributes = MessageAttributes("unknown")))
        }
    }
}
