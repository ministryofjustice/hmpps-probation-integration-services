package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.timeout
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.data.generator.OffenderDeltaGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.OffenderDeltaPoller
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDelta
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaService
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class IntegrationTest @Autowired constructor(
    @Value("\${messaging.producer.topic}") private val topicName: String,
    private val channelManager: HmppsChannelManager,
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

    @ParameterizedTest
    @MethodSource("deltas")
    fun `offender delta test`(delta: OffenderDelta, expected: List<Map<String, String>>) {
        offenderDeltaRepository.save(delta)
        verify(offenderDeltaService, after(250).atLeastOnce()).notify(any())

        offenderDeltaPoller.poll()
        generateSequence { channelManager.getChannel(topicName).receive() }.toList()

        if (expected.isNotEmpty()) {

            val propertiesCaptor = argumentCaptor<Map<String, String>>()

            verify(telemetryService, timeout(30000).atLeast(expected.size)).trackEvent(
                eq("OffenderEventPublished"),
                propertiesCaptor.capture(),
                any()
            )

            val publishedEventTypes = propertiesCaptor.allValues.mapNotNull { it["eventType"] }

            assertThat(
                publishedEventTypes,
                hasItems(*expected.map { it["eventType"] }.toTypedArray())
            )
        } else {
            verify(telemetryService, never()).trackEvent(any(), any(), any())
        }
    }

    @Test
    fun `CONTACT UPSERT with visor flag false publishes notification and telemetry but no MAPPA domain event`() {
        // given
        val delta = OffenderDeltaGenerator.generate(
            sourceTable = "CONTACT",
            sourceId = 101, // visorExported = false, softDeleted = false
            action = "UPSERT"
        )

        offenderDeltaRepository.save(delta)

        // when
        offenderDeltaPoller.poll()

        // then
        val domainEvents = domainEventRepository.findAll()
        assertThat(
            domainEvents.map { it.type.code },
            org.hamcrest.Matchers.not(
                hasItems(
                    "probation-case.mappa-information.updated"
                )
            )
        )

        val publishedEvents = generateSequence {
            channelManager.getChannel(topicName).receive()
        }.toList()

        assertThat(
            publishedEvents.map { it.eventType },
            hasItems("CONTACT_CHANGED")
        )

        verify(telemetryService, atLeastOnce()).trackEvent(
            eq("OffenderEventPublished"),
            check { properties ->
                assertThat(properties["crn"], equalTo("X123456"))
                assertThat(properties["eventType"], equalTo("CONTACT_CHANGED"))
                assertThat(properties["occurredAt"], notNullValue())
                assertThat(properties["notification"], notNullValue())
            },
            any()
        )
    }

    @Test
    fun `CONTACT UPSERT with visor flag true publishes MAPPA updated domain event, notification and telemetry`() {
        // given
        val delta = OffenderDeltaGenerator.generate(
            sourceTable = "CONTACT",
            sourceId = 202, // visorExported = true, softDeleted = false, MAPPA registration exists
            action = "UPSERT"
        )

        offenderDeltaRepository.save(delta)

        // when
        offenderDeltaPoller.poll()

        // then
        val domainEvents = domainEventRepository.findAll()

        assertThat(
            domainEvents.map { it.type.code },
            hasItems("probation-case.mappa-information.updated")
        )

        val publishedNotifications = generateSequence {
            channelManager.getChannel(topicName).receive()
        }.toList()

        assertThat(
            publishedNotifications.map { it.eventType },
            hasItems("CONTACT_CHANGED")
        )

        verify(telemetryService, atLeastOnce()).trackEvent(
            eq("OffenderEventPublished"),
            check { properties ->
                assertThat(properties["crn"], equalTo("X123456"))
                assertThat(properties["eventType"], equalTo("CONTACT_CHANGED"))
                assertThat(properties["occurredAt"], notNullValue())
                assertThat(properties["notification"], notNullValue())
            },
            any()
        )
    }

    @Test
    fun `CONTACT DELETE with visor flag false publishes CONTACT_DELETED notification and telemetry but no MAPPA domain event`() {
        // given
        val delta = OffenderDeltaGenerator.generate(
            sourceTable = "CONTACT",
            sourceId = 102, // softDeleted = true, visorExported = false
            action = "DELETE"
        )

        offenderDeltaRepository.save(delta)

        // when
        offenderDeltaPoller.poll()

        // then
        val domainEvents = domainEventRepository.findAll()
        assertThat(
            domainEvents.map { it.type.code },
            org.hamcrest.Matchers.not(
                hasItems(
                    "probation-case.mappa-information.deleted",
                    "probation-case.mappa-information.updated"
                )
            )
        )

        val publishedNotifications = generateSequence {
            channelManager.getChannel(topicName).receive()
        }.toList()

        assertThat(
            publishedNotifications.map { it.eventType },
            hasItems("CONTACT_DELETED")
        )

        verify(telemetryService, atLeastOnce()).trackEvent(
            eq("OffenderEventPublished"),
            check { properties ->
                assertThat(properties["crn"], equalTo("X123456"))
                assertThat(properties["eventType"], equalTo("CONTACT_DELETED"))
                assertThat(properties["occurredAt"], notNullValue())
                assertThat(properties["notification"], notNullValue())
            },
            any()
        )
    }

    @Test
    fun `CONTACT DELETE with visor flag true publishes CONTACT_DELETED notification telemetry and MAPPA deleted domain event`() {
        // given
        val delta = OffenderDeltaGenerator.generate(
            sourceTable = "CONTACT",
            sourceId = 201, // visorExported = true, softDeleted = true
            action = "DELETE"
        )

        offenderDeltaRepository.save(delta)

        // when
        offenderDeltaPoller.poll()

        // then
        val domainEvents = domainEventRepository.findAll()
        val eventTypes = domainEvents.map { it.type.code }

        assertThat(
            eventTypes,
            hasItems("probation-case.mappa-information.deleted")
        )

        assertThat(
            eventTypes,
            org.hamcrest.Matchers.not(
                hasItems("probation-case.mappa-information.updated")
            )
        )

        val publishedNotifications = generateSequence {
            channelManager.getChannel(topicName).receive()
        }.toList()

        assertThat(
            publishedNotifications.map { it.eventType },
            hasItems("CONTACT_DELETED")
        )

        verify(telemetryService, atLeastOnce()).trackEvent(
            eq("OffenderEventPublished"),
            check { properties ->
                assertThat(properties["crn"], equalTo("X123456"))
                assertThat(properties["eventType"], equalTo("CONTACT_DELETED"))
                assertThat(properties["occurredAt"], notNullValue())
                assertThat(properties["notification"], notNullValue())
            },
            any()
        )
    }

    /**
     * NOTE – Interim solution
     *
     * This test documents the current behaviour for hard-deleted CONTACT records.
     *
     * In hard delete scenarios the CONTACT record no longer exists in Delius,
     * therefore the VISOR flag cannot be resolved.
     *
     * As a result:
     * - No MAPPA domain event is generated for CONTACT_DELETED
     * - A CONTACT_DELETED notification is still published
     * - Telemetry is still recorded
     *
     * This behaviour is intentional and mirrors the unit-level contract.
     * It represents an interim solution and must be revisited if/when a reliable
     * way of resolving VISOR state for hard-deleted contacts is introduced.
     */
    @Test
    fun `CONTACT DELETE hard deleted contact publishes CONTACT_DELETED notification and telemetry but no MAPPA domain event`() {
        // given
        // sourceId = 99 -> hard deleted contact (no CONTACT row exists)
        val delta = OffenderDeltaGenerator.generate(
            sourceTable = "CONTACT",
            sourceId = 99,
            action = "DELETE"
        )

        offenderDeltaRepository.save(delta)

        // when
        offenderDeltaPoller.poll()

        // then
        val domainEvents = domainEventRepository.findAll()
        assertThat(
            domainEvents.map { it.type.code },
            not(
                hasItems(
                    "probation-case.mappa-information.updated",
                    "probation-case.mappa-information.deleted"
                )
            )
        )

        val publishedNotifications = generateSequence {
            channelManager.getChannel(topicName).receive()
        }.toList()

        assertThat(
            publishedNotifications.map { it.eventType },
            hasItems("CONTACT_DELETED")
        )

        verify(telemetryService, atLeastOnce()).trackEvent(
            eq("OffenderEventPublished"),
            check { properties ->
                assertThat(properties["crn"], equalTo("X123456"))
                assertThat(properties["eventType"], equalTo("CONTACT_DELETED"))
                assertThat(properties["occurredAt"], notNullValue())
                assertThat(properties["notification"], notNullValue())
            },
            any()
        )
    }

    companion object {
        private val properties = mapOf("crn" to "X123456")

        @JvmStatic
        private fun deltas() = listOf(
            Arguments.of(
                OffenderDeltaGenerator.generate(),
                listOf(
                    properties + ("eventType" to "OFFENDER_CHANGED"),
                    properties + ("eventType" to "OFFENDER_DETAILS_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "ALIAS", sourceId = 99),
                listOf(
                    properties + ("eventType" to "OFFENDER_CHANGED"),
                    properties + ("eventType" to "OFFENDER_ALIAS_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "DEREGISTRATION", sourceId = 99),
                listOf(
                    properties + ("eventType" to "OFFENDER_REGISTRATION_DEREGISTERED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "DISPOSAL", sourceId = 99),
                listOf(
                    properties + ("eventType" to "SENTENCE_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "EVENT", sourceId = 99),
                listOf(
                    properties + ("eventType" to "CONVICTION_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "MANAGEMENT_TIER_EVENT", sourceId = 99),
                emptyList<Map<String, String>>()
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "MERGE_HISTORY", sourceId = 99),
                listOf(
                    properties + ("eventType" to "OFFENDER_MERGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "OFFENDER_MANAGER", sourceId = 99),
                listOf(
                    properties + ("eventType" to "OFFENDER_CHANGED"),
                    properties + ("eventType" to "OFFENDER_MANAGER_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "OFFICER", sourceId = 99),
                listOf(
                    properties + ("eventType" to "OFFENDER_CHANGED"),
                    properties + ("eventType" to "OFFENDER_OFFICER_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "OGRS_ASSESSMENT", sourceId = 99),
                listOf(
                    properties + ("eventType" to "OFFENDER_OGRS_ASSESSMENT_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "REGISTRATION", sourceId = 99),
                listOf(
                    properties + ("eventType" to "OFFENDER_REGISTRATION_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "RQMNT", sourceId = 99),
                listOf(
                    properties + ("eventType" to "SENTENCE_ORDER_REQUIREMENT_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "REGISTRATION", sourceId = 99, action = "DELETE"),
                listOf(
                    properties + ("eventType" to "OFFENDER_REGISTRATION_DELETED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "CONTACT", sourceId = 101),
                listOf(
                    properties + ("eventType" to "CONTACT_CHANGED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "CONTACT", sourceId = 99, action = "DELETE"),
                listOf(
                    properties + ("eventType" to "CONTACT_DELETED")
                )
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "CONTACT", sourceId = 102, action = "DELETE"),
                listOf(
                    properties + ("eventType" to "CONTACT_DELETED")
                )
            )
        )
    }
}
