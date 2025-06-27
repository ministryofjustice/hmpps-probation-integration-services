package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.integrations.tier.TierCalculation
import uk.gov.justice.digital.hmpps.service.TierUpdateService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.prepMessage
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime.now

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var detailService: DomainEventDetailService

    @Mock
    lateinit var tierUpdateService: TierUpdateService

    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `should update tier`() {
        // Given a message
        val message = prepMessage("tier-calculation", 1234)
        // And a calculation
        val calculation = TierCalculation("someScore", "calculationId", now())
        whenever(detailService.getDetail<Any>(anyOrNull(), anyOrNull())).thenReturn(calculation)

        // When the message is received
        handler.handle(message)

        // Then it is updated in Delius and logged to Telemetry
        verify(telemetryService).notificationReceived(message)
        verify(detailService).getDetail<Any>(anyOrNull(), anyOrNull())
        verify(tierUpdateService).updateTier("A000001", calculation)
        verify(telemetryService).trackEvent("TierUpdateSuccess", calculation.telemetryProperties("A000001"))
    }
}
