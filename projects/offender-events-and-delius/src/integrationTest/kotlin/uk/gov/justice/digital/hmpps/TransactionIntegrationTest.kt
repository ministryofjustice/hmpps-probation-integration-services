package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
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
internal class TransactionIntegrationTest @Autowired constructor(
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
        domainEventRepository.deleteAll()
        offenderDeltaRepository.deleteAll()
    }

    @Test
    fun `when all deltas prepare successfully then everything is committed`() {
        // GIVEN
        whenever(contactRepository.existsByIdAndVisorContactTrue(1001L)).thenReturn(true)
        whenever(contactRepository.existsByIdAndSoftDeletedFalse(1001L)).thenReturn(true)
        offenderDeltaRepository.saveAll(
            listOf(
                OffenderDeltaGenerator.generate(sourceTable = "OFFENDER", sourceId = 1000, action = "UPSERT"),
                OffenderDeltaGenerator.generate(sourceTable = "CONTACT", sourceId = 1001, action = "UPSERT"),
                OffenderDeltaGenerator.generate(sourceTable = "OFFENDER", sourceId = 1002, action = "UPSERT"),
            )
        )

        // WHEN
        offenderDeltaPoller.poll()

        // THEN

        // SNS messages published
        verify(notificationPublisher, atLeastOnce()).publish(any())

        // Telemetry recorded
        verify(telemetryService, atLeastOnce()).trackEvent(eq("OffenderEventPublished"), any(), any())

        // Domain events persisted
        assertThat(domainEventRepository.findAll().map { it.type.code })
            .isEqualTo(listOf("probation-case.mappa-information.updated"))

        // offender_delta records deleted
        assertThat(offenderDeltaRepository.count()).isEqualTo(0)
    }

    @Test
    fun `when prepare fails then nothing is committed for any delta`() {
        // GIVEN
        whenever(contactRepository.existsByIdAndSoftDeletedFalse(2001L)).thenReturn(true)
        whenever(contactRepository.existsByIdAndVisorContactTrue(2001L))
            .thenThrow(RuntimeException("Simulated failure during prepare phase"))
        offenderDeltaRepository.saveAll(
            listOf(
                OffenderDeltaGenerator.generate(sourceTable = "OFFENDER", sourceId = 2000, action = "UPSERT"),
                OffenderDeltaGenerator.generate(sourceTable = "CONTACT", sourceId = 2001, action = "UPSERT"),
                OffenderDeltaGenerator.generate(sourceTable = "OFFENDER", sourceId = 2002, action = "UPSERT"),
            )
        )

        val idsBeforePoll = offenderDeltaRepository.findAll().map { it.id }.sorted()
        val domainEventsBefore = domainEventRepository.count()


        // WHEN
        assertThrows(RuntimeException::class.java) { offenderDeltaPoller.poll() }

        // THEN

        // No SNS publish at all
        verify(notificationPublisher, never()).publish(any())

        // No domain events persisted
        assertThat(domainEventRepository.count()).isEqualTo(domainEventsBefore)

        // Transaction rollback: offender_delta unchanged
        val idsAfterPoll = offenderDeltaRepository.findAll().map { it.id }.sorted()
        assertThat(idsAfterPoll).isEqualTo(idsBeforePoll)
    }
}
