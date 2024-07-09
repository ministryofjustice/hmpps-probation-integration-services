package uk.gov.justice.digital.hmpps.scheduling

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.service.DomainEventService

@ExtendWith(MockitoExtension::class)
class SchedulerTest {
    @Mock
    private lateinit var domainEventService: DomainEventService

    @InjectMocks
    private lateinit var scheduler: Scheduler

    @Test
    fun `failure to publish is thrown`() {
        whenever(domainEventService.getDeltas()).thenThrow(RuntimeException("event not processed"))
        assertThrows<RuntimeException> { scheduler.poll() }
    }
}
