package uk.gov.justice.digital.hmpps.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.anyMap
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.message.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.hmppsEventReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class TelemetryMessagingExtensionsTest {

    @Mock
    private lateinit var telemetryClient: TelemetryClient

    private lateinit var telemetryService: TelemetryService

    @BeforeEach
    fun setup() {
        telemetryService = TelemetryService(telemetryClient)
    }

    @Test
    fun test() {
        val eventType = "some.special.event"
        val detailUrl = "https://detail/url"

        telemetryService.notificationReceived(
            Notification(
                HmppsDomainEvent(
                    eventType,
                    1,
                    detailUrl,
                    ZonedDateTime.parse("2022-08-09T12:23:43.000+01:00[Europe/London]"),
                    personReference = PersonReference(listOf(PersonIdentifier("CRN", "X12345")))
                )
            )
        )

        verify(telemetryClient).trackEvent(
            eq("NotificationReceived"),
            check {
                assertThat(it["eventType"], equalTo(eventType))
                assertThat(it["detailUrl"], equalTo(detailUrl))
                assertThat(it["CRN"], equalTo("X12345"))
            },
            anyMap()
        )
    }

    @Test
    fun `null detail url is ignored`() {
        telemetryService.hmppsEventReceived(
            HmppsDomainEvent(
                eventType = "some.special.event",
                version = 1,
                occurredAt = ZonedDateTime.now()
            )
        )

        verify(telemetryClient).trackEvent(
            eq("NotificationReceived"),
            check { assertThat(it, not(hasProperty("detailUrl"))) },
            anyMap()
        )
    }

    @Test
    fun `handles other types of notification (non domain events)`() {
        telemetryService.notificationReceived(
            Notification(
                message = "this is a string",
                attributes = MessageAttributes("test.event")
            )
        )

        verify(telemetryClient).trackEvent(eq("NotificationReceived"), anyMap(), anyMap())
    }

    @Test
    fun `handles events with no event type`() {
        telemetryService.notificationReceived(Notification(message = "this is a string"))

        verify(telemetryClient).trackEvent(eq("NotificationReceived"), anyMap(), anyMap())
    }
}
