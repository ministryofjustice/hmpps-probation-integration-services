package uk.gov.justice.digital.hmpps.scheduling

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.service.DomainEventService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
class SchedulerTest {
    @Mock
    private lateinit var domainEventService: DomainEventService

    @Mock
    private lateinit var telemetryService: TelemetryService

    @InjectMocks
    private lateinit var scheduler: Scheduler

    @Test
    fun `failure to publish is thrown`() {
        whenever(domainEventService.publishBatch()).thenThrow(RuntimeException("event not processed"))
        assertThrows<RuntimeException> { scheduler.poll() }
    }

    @Test
    fun `poller receives no events and doesn't call telemetry`() {
        whenever(domainEventService.publishBatch()).thenReturn(0)
        scheduler.poll()
        verify(telemetryService, never()).trackEvent(any(), any(), any())
    }

    @Test
    fun `poller calls telemetry on success`() {
        whenever(domainEventService.publishBatch()).thenReturn(100)
        scheduler.poll()
        verify(telemetryService).trackEvent("DomainEventsProcessed", mapOf("EventsSent" to "100"), mapOf())
    }
}
