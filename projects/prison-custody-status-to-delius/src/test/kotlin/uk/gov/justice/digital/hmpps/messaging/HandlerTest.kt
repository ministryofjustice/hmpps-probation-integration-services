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
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseService
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttribute
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
            "prison-offender-events.prisoner.released",
            1,
            "https//detail/url",
            ZonedDateTime.now(),
            additionalInformation = AdditionalInformation(
                mutableMapOf(
                    "nomsNumber" to "Z0001ZZ",
                    "prisonId" to "ZZZ",
                    "reason" to "Test data",
                    "nomisMovementReasonCode" to "OPA",
                    "details" to "Test data"
                )
            )
        ),
        attributes = MessageAttributes("prison-offender-events.prisoner.released")
    )

    @Test
    fun messageIsLoggedToTelemetry() {
        whenever(
            releaseService.release(
                notification.message.additionalInformation.nomsNumber(),
                notification.message.additionalInformation.prisonId(),
                notification.message.additionalInformation.reason(),
                notification.message.additionalInformation.movementReason(),
                notification.message.occurredAt
            )
        ).thenReturn(ReleaseOutcome.PrisonerReleased)
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)
    }

    @Test
    fun releaseMessagesAreHandled() {
        whenever(
            releaseService.release(
                notification.message.additionalInformation.nomsNumber(),
                notification.message.additionalInformation.prisonId(),
                notification.message.additionalInformation.reason(),
                notification.message.additionalInformation.movementReason(),
                notification.message.occurredAt
            )
        ).thenReturn(ReleaseOutcome.PrisonerReleased)

        handler.handle(notification)
        verify(releaseService).release(
            notification.message.additionalInformation.nomsNumber(),
            notification.message.additionalInformation.prisonId(),
            notification.message.additionalInformation.reason(),
            notification.message.additionalInformation.movementReason(),
            notification.message.occurredAt
        )
    }

    @Test
    fun recallMessagesAreHandled() {
        whenever(recallService.recall("Z0001ZZ", "ZZZ", "Test data", "OPA", notification.message.occurredAt))
            .thenReturn(RecallOutcome.PrisonerRecalled)
        val attrs = MessageAttributes("prison-offender-events.prisoner.received")
        attrs["nomisMovementReasonCode"] = MessageAttribute("String", "R1")
        handler.handle(notification.copy(attributes = attrs))
        verify(recallService).recall("Z0001ZZ", "ZZZ", "Test data", "OPA", notification.message.occurredAt)
        verify(telemetryService).trackEvent("PrisonerRecalled", notification.message.telemetryProperties())
    }

    @Test
    fun unknownMessagesAreThrown() {
        assertThrows<IllegalArgumentException> {
            handler.handle(notification.copy(attributes = MessageAttributes("unknown")))
        }
    }
}
