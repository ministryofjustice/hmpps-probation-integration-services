package uk.gov.justice.digital.hmpps.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.anyMap
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.message.HmppsEvent
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
    @Suppress("UNCHECKED_CAST")
    fun test() {
        val eventType = "some.special.event"
        val detailUrl = "https://detail/url"

        telemetryService.hmppsEventReceived(
            HmppsEvent(
                eventType,
                1,
                detailUrl,
                ZonedDateTime.parse("2022-08-09T12:23:43.000+01:00[Europe/London]"),
                personReference = PersonReference(listOf(PersonIdentifier("CRN", "X12345")))
            )
        )

        val propertyCaptor = ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, String>>
        verify(telemetryClient).trackEvent(
            eq("SOME_SPECIAL_EVENT_RECEIVED"),
            propertyCaptor.capture(),
            anyMap()
        )

        val props = propertyCaptor.value
        assertThat(props["eventType"], equalTo(eventType))
        assertThat(props["detailUrl"], equalTo(detailUrl))
        assertThat(props["CRN"], equalTo("X12345"))
    }
}
