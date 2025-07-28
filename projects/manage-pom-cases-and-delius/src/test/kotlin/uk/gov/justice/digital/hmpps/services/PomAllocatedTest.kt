package uk.gov.justice.digital.hmpps.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class PomAllocatedTest {

    @Mock
    private lateinit var detailService: DomainEventDetailService

    @Mock
    private lateinit var personRepository: PersonRepository

    @Mock
    private lateinit var prisonManagerService: PrisonManagerService

    @Mock
    private lateinit var telemetryService: TelemetryService

    private lateinit var service: PomAllocated

    @BeforeEach
    fun setUp() {
        service = PomAllocated(detailService, personRepository, prisonManagerService, telemetryService, "localhost")
    }

    @Test
    fun `NotImplementedError thrown if unexpected message received`() {
        val event = HmppsDomainEvent(
            eventType = "unknown.message",
            version = 1
        )

        assertThrows<NotImplementedError> {
            service.process(event)
        }
    }

    @Test
    fun `ignore prison identifier messages if no noms number included`() {
        val event = HmppsDomainEvent(
            eventType = "probation-case.prison-identifier.added",
            version = 1,
            personReference = PersonReference(emptyList())
        )

        service.process(event)

        verify(prisonManagerService, never()).allocatePrisonManager(any(), any(), any())
        verify(prisonManagerService, never()).deallocatePrisonManager(any(), any())
        verify(telemetryService).trackEvent("NOMS Number not found on ${event.eventType} event", mapOf("nomsId" to "Not Provided"), mapOf())
    }
}