package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.OffenderDeltaGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDelta
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderEvent
import uk.gov.justice.digital.hmpps.integrations.delius.offender.asNotifications
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.retry.retry
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
    fun `offender delta test`(delta: OffenderDelta, notifications: List<Notification<OffenderEvent>>) {
        offenderDeltaRepository.save(delta)
        notifications.forEach {
            val notification = jmsTemplate.receiveAndConvert(topicName)
            assertEquals(it, notification)
        }
        retry(3) { offenderDeltaRepository.findById(delta.id).isEmpty }
        assertEquals(0, offenderDeltaRepository.count())
    }

    companion object {
        private val deltas = listOf(
            OffenderDeltaGenerator.generate(),
            OffenderDeltaGenerator.generate(sourceTable = "ALIAS", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "DEREGISTRATION", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "DISPOSAL", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "EVENT", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "MANAGEMENT_TIER_EVENT", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "MERGE_HISTORY", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "OFFENDER", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "OFFICER", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "OGRS_ASSESSMENT", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "REGISTRATION", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "RQMNT", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "REGISTRATION", sourceId = 99, action = "DELETE"),
        )

        @JvmStatic
        fun deltas(): List<Arguments> = deltas.map {
            Arguments.of(it, it.asNotifications())
        }
    }
}
