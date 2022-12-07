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
import org.mockito.Mockito.any
import org.mockito.Mockito.anyMap
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
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

        telemetryService.hmppsEventReceived(
            HmppsDomainEvent(
                eventType,
                1,
                detailUrl,
                ZonedDateTime.parse("2022-08-09T12:23:43.000+01:00[Europe/London]"),
                personReference = PersonReference(listOf(PersonIdentifier("CRN", "X12345")))
            )
        )

        val propertyCaptor = argumentCaptor<Map<String, String>>()
        verify(telemetryClient).trackEvent(
            eq("SOME_SPECIAL_EVENT_RECEIVED"),
            propertyCaptor.capture(),
            anyMap()
        )

        val props = propertyCaptor.firstValue
        assertThat(props["eventType"], equalTo(eventType))
        assertThat(props["detailUrl"], equalTo(detailUrl))
        assertThat(props["CRN"], equalTo("X12345"))
    }

    @Test
    fun nullDetailUrlIsIgnored() {
        telemetryService.hmppsEventReceived(
            HmppsDomainEvent(
                eventType = "some.special.event",
                version = 1,
                occurredAt = ZonedDateTime.now()
            )
        )

        val propertyCaptor = argumentCaptor<Map<String, String>>()
        verify(telemetryClient).trackEvent(any(), propertyCaptor.capture(), anyMap())

        assertThat(propertyCaptor.firstValue, not(hasProperty("detailUrl")))
    }
}
