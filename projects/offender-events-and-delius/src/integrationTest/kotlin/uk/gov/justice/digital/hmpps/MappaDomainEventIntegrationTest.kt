package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderDeltaGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.OffenderDeltaPoller
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class MappaDomainEventIntegrationTest @Autowired constructor(
    private val offenderDeltaPoller: OffenderDeltaPoller,
    private val offenderDeltaRepository: OffenderDeltaRepository,
    private val domainEventRepository: DomainEventRepository
) {
    @MockitoSpyBean
    lateinit var offenderDeltaService: OffenderDeltaService

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @BeforeEach
    fun reset() {
        domainEventRepository.deleteAll()
    }

    @Test
    fun `Contact upsert with visor flag false does not publish MAPPA domain event`() {
        // given
        offenderDeltaRepository.save(contact(ContactGenerator.DEFAULT.id, action = "UPSERT"))

        // when
        offenderDeltaPoller.poll()

        // then
        val domainEvents = domainEventRepository.findAll()
        assertThat(domainEvents.map { it.type.code }).doesNotContain("probation-case.mappa-information.updated")
    }

    @Test
    fun `Contact upsert with visor flag true publishes MAPPA updated domain event`() {
        // given
        offenderDeltaRepository.save(contact(ContactGenerator.VISOR.id, action = "UPSERT"))

        // when
        offenderDeltaPoller.poll()

        // then
        val domainEvents = domainEventRepository.findAll()
        assertThat(domainEvents.map { it.type.code }).isEqualTo(listOf("probation-case.mappa-information.updated"))
    }

    @Test
    fun `Contact delete with visor flag false does not publish MAPPA domain event`() {
        // given
        offenderDeltaRepository.save(contact(ContactGenerator.DELETED.id, action = "DELETE"))

        // when
        offenderDeltaPoller.poll()

        // then
        val domainEvents = domainEventRepository.findAll()
        assertThat(domainEvents.map { it.type.code })
            .doesNotContain("probation-case.mappa-information.updated")
            .doesNotContain("probation-case.mappa-information.deleted")
    }

    @Test
    fun `Contact delete with visor flag true publishes MAPPA deleted domain event`() {
        // given
        offenderDeltaRepository.save(contact(ContactGenerator.DELETED_VISOR.id, action = "DELETE"))

        // when
        offenderDeltaPoller.poll()

        // then
        val domainEvents = domainEventRepository.findAll()
        assertThat(domainEvents.map { it.type.code }).isEqualTo(listOf("probation-case.mappa-information.deleted"))
    }

    /**
     * This test documents the current behaviour for hard-deleted CONTACT records.
     *
     * In hard delete scenarios the CONTACT record no longer exists in Delius,
     * therefore the VISOR flag cannot be resolved.
     *
     * As a result:
     * - No MAPPA domain event is generated for CONTACT_DELETED
     */
    @Test
    fun `Hard deleted contact publishes no MAPPA domain event`() {
        // given
        val nonexistentId = 99999L
        offenderDeltaRepository.save(contact(nonexistentId, action = "DELETE"))

        // when
        offenderDeltaPoller.poll()

        // then
        val domainEvents = domainEventRepository.findAll()
        assertThat(domainEvents.map { it.type.code })
            .doesNotContain("probation-case.mappa-information.updated")
            .doesNotContain("probation-case.mappa-information.deleted")
    }

    companion object {
        @JvmStatic
        private fun contact(id: Long, action: String = "UPSERT") = OffenderDeltaGenerator.generate(
            sourceTable = "CONTACT",
            sourceId = id,
            action = action
        )
    }
}
