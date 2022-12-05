package uk.gov.justice.digital.hmpps.integrations.delius

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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
        whenever(service.checkAndSendEvents()).thenReturn(Pair(0, 0))
        poller.checkAndSendEvents()
        verify(telemetryService, never()).trackEvent(any(), any(), any())
    }

    @ParameterizedTest
    @MethodSource("counts")
    fun `poller calls telemetry if either events or not found`(
        counts: Pair<Int, Int>,
        telemetryProperties: Map<String, String>
    ) {
        whenever(service.checkAndSendEvents()).thenReturn(counts)

        poller.checkAndSendEvents()
        verify(telemetryService).trackEvent(
            "OffenderEventsProcessed",
            telemetryProperties,
            mapOf()
        )
    }

    companion object {
        @JvmStatic
        private fun counts() = listOf(
            Arguments.of(Pair(0, 1), mapOf("EventsSent" to "0", "PersonNotFound" to "1")),
            Arguments.of(Pair(1, 0), mapOf("EventsSent" to "1", "PersonNotFound" to "0")),
            Arguments.of(Pair(1, 1), mapOf("EventsSent" to "1", "PersonNotFound" to "1")),
        )
    }
}
