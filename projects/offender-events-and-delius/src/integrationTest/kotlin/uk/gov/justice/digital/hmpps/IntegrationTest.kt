package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.after
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.justice.digital.hmpps.data.generator.OffenderDeltaGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDelta
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaService
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class IntegrationTest {
    @Value("\${messaging.producer.topic}")
    lateinit var topicName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var offenderDeltaRepository: OffenderDeltaRepository

    @SpyBean
    lateinit var offenderDeltaService: OffenderDeltaService

    @MockBean
    lateinit var telemetryService: TelemetryService

    @ParameterizedTest
    @MethodSource("deltas")
    fun `offender delta test`(delta: OffenderDelta, expected: List<String>) {
        offenderDeltaRepository.save(delta)

        verify(offenderDeltaService, after(250).atLeastOnce()).checkAndSendEvents()
        val received = generateSequence { channelManager.getChannel(topicName).receive()?.eventType }.toList()

        assertEquals(expected.sorted(), received.sorted())
        if (expected.isNotEmpty()) {
            verify(telemetryService).trackEvent(eq("OffenderEventsProcessed"), any(), any())
        } else {
            verify(telemetryService, never()).trackEvent(any(), any(), any())
        }
    }

    companion object {
        @JvmStatic
        private fun deltas() = listOf(
            Arguments.of(
                OffenderDeltaGenerator.generate(),
                listOf("OFFENDER_CHANGED", "OFFENDER_DETAILS_CHANGED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "ALIAS", sourceId = 99),
                listOf("OFFENDER_CHANGED", "OFFENDER_ALIAS_CHANGED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "DEREGISTRATION", sourceId = 99),
                listOf("OFFENDER_REGISTRATION_DEREGISTERED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "DISPOSAL", sourceId = 99),
                listOf("SENTENCE_CHANGED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "EVENT", sourceId = 99),
                listOf("CONVICTION_CHANGED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "MANAGEMENT_TIER_EVENT", sourceId = 99),
                listOf("OFFENDER_MANAGEMENT_TIER_CALCULATION_REQUIRED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "MERGE_HISTORY", sourceId = 99),
                listOf("OFFENDER_MERGED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "OFFENDER_MANAGER", sourceId = 99),
                listOf("OFFENDER_CHANGED", "OFFENDER_MANAGER_CHANGED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "OFFICER", sourceId = 99),
                listOf("OFFENDER_CHANGED", "OFFENDER_OFFICER_CHANGED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "OGRS_ASSESSMENT", sourceId = 99),
                listOf("OFFENDER_OGRS_ASSESSMENT_CHANGED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "REGISTRATION", sourceId = 99),
                listOf("OFFENDER_REGISTRATION_CHANGED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "RQMNT", sourceId = 99),
                listOf("SENTENCE_ORDER_REQUIREMENT_CHANGED")
            ),
            Arguments.of(
                OffenderDeltaGenerator.generate(sourceTable = "REGISTRATION", sourceId = 99, action = "DELETE"),
                listOf("OFFENDER_REGISTRATION_DELETED")
            )
        )
    }
}
