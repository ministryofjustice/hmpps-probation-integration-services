package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
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
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.offender.asNotifications
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

    @Test
    fun `offender delta batching test`() {
        offenderDeltaRepository.saveAll(deltas)
        val notifications = deltas.flatMap { it.asNotifications() }
        val messages = (1..notifications.size).map {
            jmsTemplate.receiveAndConvert(topicName)
        }
        assertEquals(17, messages.size)
        assertTrue(messages.containsAll(notifications))
        verify(telemetryService, timeout(5000).times(3)).trackEvent(any(), any(), any())
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
            OffenderDeltaGenerator.generate(sourceTable = "OFFENDER_MANAGER", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "OFFICER", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "OGRS_ASSESSMENT", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "REGISTRATION", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "RQMNT", sourceId = 99),
            OffenderDeltaGenerator.generate(sourceTable = "REGISTRATION", sourceId = 99, action = "DELETE"),
        )
    }
}
