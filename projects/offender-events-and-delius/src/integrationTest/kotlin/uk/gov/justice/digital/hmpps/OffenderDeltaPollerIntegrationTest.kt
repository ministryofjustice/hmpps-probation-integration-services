package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.OffenderDeltaGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.OffenderDeltaPoller
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.offender.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaRepository
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class OffenderDeltaPollerIntegrationTest @Autowired constructor(
    @Value("\${messaging.producer.topic}") private val topicName: String,
    private val offenderDeltaPoller: OffenderDeltaPoller,
    private val offenderDeltaRepository: OffenderDeltaRepository,
    private val domainEventRepository: DomainEventRepository,
) {

    @MockitoBean
    lateinit var notificationPublisher: NotificationPublisher

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @MockitoBean
    lateinit var contactRepository: ContactRepository

    @BeforeEach
    fun setUp() {
        offenderDeltaRepository.deleteAll()
    }

    @Test
    fun `when all deltas prepare successfully then everything is committed`() {
        // GIVEN
        val delta1 = OffenderDeltaGenerator.generate(
            sourceTable = "OFFENDER",
            sourceId = 99,
            action = "UPSERT"
        )

        val delta2 = OffenderDeltaGenerator.generate(
            sourceTable = "CONTACT",
            sourceId = 101,
            action = "UPSERT"
        )

        val delta3 = OffenderDeltaGenerator.generate(
            sourceTable = "OFFENDER",
            sourceId = 98,
            action = "UPSERT"
        )

        offenderDeltaRepository.saveAll(listOf(delta1, delta2, delta3))

        whenever(contactRepository.existsByIdAndVisorContactTrue(101L))
            .thenReturn(true)

        whenever(contactRepository.existsByIdAndSoftDeletedFalse(101L))
            .thenReturn(true)

        // WHEN
        offenderDeltaPoller.poll()

        // THEN

        // SNS messages published
        verify(notificationPublisher, atLeast(1)).publish(any())

        // Telemetry recorded
        verify(telemetryService, atLeastOnce()).trackEvent(eq("OffenderEventPublished"), any(), any())

        // Domain events persisted
        val domainEvents = domainEventRepository.findAll()
        assertEquals(1, domainEvents.size)
        assertEquals("probation-case.mappa-information.updated", domainEvents.first().type.code)

        // offender_delta records deleted
        assertEquals(0, offenderDeltaRepository.count())
    }

    @Test
    fun `when prepare fails then nothing is committed for any delta`() {
        // GIVEN
        val delta1 = OffenderDeltaGenerator.generate(
            sourceTable = "OFFENDER",
            sourceId = 99,
            action = "UPSERT"
        )

        val delta2 = OffenderDeltaGenerator.generate(
            sourceTable = "CONTACT",
            sourceId = 101,
            action = "UPSERT"
        )

        val delta3 = OffenderDeltaGenerator.generate(
            sourceTable = "OFFENDER",
            sourceId = 98,
            action = "UPSERT"
        )

        offenderDeltaRepository.saveAll(listOf(delta1, delta2, delta3))

        val idsBeforePoll = offenderDeltaRepository.findAll().map { it.id }.sorted()
        val domainEventsBefore = domainEventRepository.count()

        whenever(contactRepository.existsByIdAndVisorContactTrue(101L))
            .thenThrow(RuntimeException("Simulated failure during prepare phase"))

        // WHEN
        assertThrows(RuntimeException::class.java) { offenderDeltaPoller.poll() }

        // THEN

        // No SNS publish at all
        verify(notificationPublisher, times(0)).publish(any())

        // No domain events persisted
        assertEquals(domainEventsBefore, domainEventRepository.count())

        // Transaction rollback: offender_delta unchanged
        val idsAfterPoll = offenderDeltaRepository.findAll().map { it.id }.sorted()
        assertEquals(3, offenderDeltaRepository.count())
        assertEquals(idsBeforePoll, idsAfterPoll)
    }
}
