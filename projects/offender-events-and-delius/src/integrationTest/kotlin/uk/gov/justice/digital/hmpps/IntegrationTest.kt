package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.OffenderDeltaGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDelta
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaRepository
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
@ActiveProfiles("integration-test")
internal class IntegrationTest {
    @Value("\${spring.jms.template.default-destination}")
    lateinit var topicName: String

    @Autowired
    lateinit var jmsTemplate: JmsTemplate

    @Autowired
    lateinit var offenderDeltaRepository: OffenderDeltaRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @ParameterizedTest
    @MethodSource("deltas")
    fun `offender delta test`(delta: OffenderDelta, eventTypes: List<String>) {
        offenderDeltaRepository.save(delta)

        val messages = (1..eventTypes.size).mapNotNull {
            (jmsTemplate.receiveAndConvert(topicName) as Notification<*>).eventType
        }

        assertEquals(eventTypes.size, messages.size)
        assertEquals(eventTypes.sorted(), messages.sorted())
        verify(telemetryService, timeout(5000)).trackEvent(any(), any(), any())
        assertEquals(0, offenderDeltaRepository.count())
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
