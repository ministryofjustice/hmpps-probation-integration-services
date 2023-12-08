package uk.gov.justice.digital.hmpps.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.anyMap
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class TelemetryServiceTest {
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
                    personReference = PersonReference(listOf(PersonIdentifier("CRN", "X12345"))),
                ),
            ),
        )

        verify(telemetryClient).trackEvent(
            eq("SOME_SPECIAL_EVENT_RECEIVED"),
            check {
                assertThat(it["eventType"], equalTo(eventType))
                assertThat(it["detailUrl"], equalTo(detailUrl))
                assertThat(it["CRN"], equalTo("X12345"))
            },
            anyMap(),
        )
    }

    @Test
    fun `null detail url is ignored`() {
        telemetryService.hmppsEventReceived(
            HmppsDomainEvent(
                eventType = "some.special.event",
                version = 1,
                occurredAt = ZonedDateTime.now(),
            ),
        )

        verify(telemetryClient).trackEvent(
            eq("SOME_SPECIAL_EVENT_RECEIVED"),
            check { assertThat(it, not(hasProperty("detailUrl"))) },
            anyMap(),
        )
    }

    @Test
    fun `handles other types of notification (non domain events)`() {
        telemetryService.notificationReceived(
            Notification(
                message = "this is a string",
                attributes = MessageAttributes("test.event"),
            ),
        )

        verify(telemetryClient).trackEvent(eq("TEST_EVENT_RECEIVED"), anyMap(), anyMap())
    }

    @Test
    fun `handles events with no event type`() {
        telemetryService.notificationReceived(Notification(message = "this is a string"))

        verify(telemetryClient).trackEvent(eq("UNKNOWN_EVENT_RECEIVED"), anyMap(), anyMap())
    }
}
