package uk.gov.justice.digital.hmpps.telemetry

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

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
    fun `track event calls the client`() {
        val eventName = "TestEvent"
        val properties = mapOf("property1" to "value1", "property2" to "value2")
        telemetryService.trackEvent(eventName, properties)

        verify(telemetryClient).trackEvent(eventName, properties, mapOf())
    }
}
