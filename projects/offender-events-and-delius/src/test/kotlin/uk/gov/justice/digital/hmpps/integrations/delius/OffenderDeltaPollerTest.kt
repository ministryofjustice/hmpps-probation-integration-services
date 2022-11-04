package uk.gov.justice.digital.hmpps.integrations.delius

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
class OffenderDeltaPollerTest {

    @Mock
    private lateinit var service: OffenderDeltaService

    @Mock
    private lateinit var telemetryService: TelemetryService

    private lateinit var poller: OffenderDeltaPoller

    @BeforeEach
    fun setup() {
        poller = OffenderDeltaPoller(service, telemetryService)
    }

    @Test
    fun `unsuccessful event creation notify app insights`() {
        whenever(service.checkAndSendEvents()).thenThrow(RuntimeException("event not processed"))
        poller.checkAndSendEvents()

        verify(telemetryService).trackEvent(
            "OffenderEventsProcessingFailed",
            mapOf("Exception" to "event not processed"), mapOf()
        )
    }

    @Test
    fun `poller receives no events and doesn't call telemetry`() {
        whenever(service.checkAndSendEvents()).thenReturn(0)
        poller.checkAndSendEvents()
        verify(telemetryService, never()).trackEvent(any(), any(), any())
    }
}
