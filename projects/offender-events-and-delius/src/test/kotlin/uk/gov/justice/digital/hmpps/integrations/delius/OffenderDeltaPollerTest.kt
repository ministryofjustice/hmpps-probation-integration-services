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
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
            mapOf("Exception" to "event not processed"),
            mapOf()
        )
    }

    @Test
    fun `poller receives no events and doesn't call telemetry`() {
        whenever(service.checkAndSendEvents()).thenReturn(listOf())
        poller.checkAndSendEvents()
        verify(telemetryService, never()).trackEvent(any(), any(), any())
    }

    @Test
    fun `poller calls telemetry if either events or not found`() {
        val dateTime = ZonedDateTime.now()
        whenever(service.checkAndSendEvents()).thenReturn(
            listOf(
                Notification(
                    OffenderEvent(123456, "X123456", null, 67891, dateTime),
                    MessageAttributes("OFFENDER_CHANGED")
                ),
                Notification(
                    OffenderEvent(123456, "X123456", null, 67891, dateTime),
                    MessageAttributes("OFFENDER_MANAGER_CHANGED")
                ),
                Notification(
                    OffenderEvent(223456, "X223456", null, 67890, dateTime),
                    MessageAttributes("ORDER_MANAGER_CHANGED")
                ),
                Notification(
                    OffenderEvent(223456, "X223456", null, 67890, dateTime),
                    MessageAttributes("CONVICTION_CHANGED")
                )
            )
        )

        poller.checkAndSendEvents()
        verify(telemetryService).trackEvent(
            "OffenderEventPublished",
            mapOf(
                "crn" to "X123456",
                "eventType" to "OFFENDER_CHANGED",
                "occurredAt" to DateTimeFormatter.ISO_ZONED_DATE_TIME.format(dateTime)
            ),
            mapOf()
        )
        verify(telemetryService).trackEvent(
            "OffenderEventPublished",
            mapOf(
                "crn" to "X123456",
                "eventType" to "OFFENDER_MANAGER_CHANGED",
                "occurredAt" to DateTimeFormatter.ISO_ZONED_DATE_TIME.format(dateTime)
            ),
            mapOf()
        )
        verify(telemetryService).trackEvent(
            "OffenderEventPublished",
            mapOf(
                "crn" to "X223456",
                "eventType" to "ORDER_MANAGER_CHANGED",
                "occurredAt" to DateTimeFormatter.ISO_ZONED_DATE_TIME.format(dateTime)
            ),
            mapOf()
        )
        verify(telemetryService).trackEvent(
            "OffenderEventPublished",
            mapOf(
                "crn" to "X223456",
                "eventType" to "CONVICTION_CHANGED",
                "occurredAt" to DateTimeFormatter.ISO_ZONED_DATE_TIME.format(dateTime)
            ),
            mapOf()
        )
    }
}
