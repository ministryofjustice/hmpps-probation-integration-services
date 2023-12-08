package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class CvlHandlerTest {
    @Mock
    internal lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @Mock
    internal lateinit var telemetryService: TelemetryService

    @Mock
    internal lateinit var licenceActivatedHandler: LicenceActivatedHandler

    @Mock
    internal lateinit var featureFlags: FeatureFlags

    @InjectMocks
    internal lateinit var handler: CvlHandler

    @BeforeEach
    fun setUp() {
        whenever(featureFlags.enabled("cvl-licence-activated")).thenReturn(true)
    }

    @Test
    fun `unexpected event types are ignored`() {
        val notification =
            Notification(
                HmppsDomainEvent("unknown.type.event-type", 1),
                MessageAttributes("unknown.type.event-type"),
            )

        handler.handle(notification)

        verify(telemetryService).trackEvent(
            "UnexpectedEventType",
            mapOf("eventType" to "unknown.type.event-type"),
            mapOf(),
        )
    }

    @Test
    fun `action results with failure throw exception`() {
        val notification =
            Notification(
                HmppsDomainEvent(DomainEventType.LicenceActivated.name, 1),
                MessageAttributes(DomainEventType.LicenceActivated.name),
            )

        whenever(licenceActivatedHandler.licenceActivated(any()))
            .thenReturn(listOf(ActionResult.Failure(RuntimeException("Unknown Exception Happened"))))

        val ex =
            assertThrows<RuntimeException> {
                handler.handle(notification)
            }
        assertThat(ex.message, equalTo("Unknown Exception Happened"))
    }
}
