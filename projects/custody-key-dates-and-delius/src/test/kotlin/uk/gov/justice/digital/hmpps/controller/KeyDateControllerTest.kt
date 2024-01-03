package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.controller.KeyDateController
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class KeyDateControllerTest {

    @Mock
    lateinit var custodyDateUpdateService: CustodyDateUpdateService

    @Mock
    lateinit var telemetryService: TelemetryService

    @InjectMocks
    lateinit var keyDateController: KeyDateController

    @Test
    fun `errors are logged to telemetry`() {
        whenever(custodyDateUpdateService.updateCustodyKeyDates(any(), any(), any()))
            .thenThrow(RuntimeException("error"))

        keyDateController.updateKeyDates(listOf("A0001AA", "A0002AA"), false)

        verify(telemetryService, timeout(5000))
            .trackEvent("KeyDateUpdateFailed", mapOf("nomsNumber" to "A0001AA", "message" to "error"), mapOf())
        verify(telemetryService, timeout(5000))
            .trackEvent("KeyDateUpdateFailed", mapOf("nomsNumber" to "A0002AA", "message" to "error"), mapOf())
    }
}
