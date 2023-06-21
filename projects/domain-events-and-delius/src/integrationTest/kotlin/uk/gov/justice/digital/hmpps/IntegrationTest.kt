package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.DomainEventGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.DomainEventRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class IntegrationTest {
    @Value("\${messaging.producer.topic}")
    lateinit var topicName: String

    @Autowired
    lateinit var hmppsChannelManager: HmppsChannelManager

    @Autowired
    lateinit var domainEventRepository: DomainEventRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `messages are published successfully`() {
        domainEventRepository.saveAll(
            listOf(
                DomainEventGenerator.generate("manual-ogrs"),
                DomainEventGenerator.generate("registration-added")
            )
        )

        val topic = hmppsChannelManager.getChannel(topicName)
        val messages = mutableListOf<String>()
        while (messages.size < 2) {
            topic.receive()?.eventType?.let { messages.add(it) }
        }

        assertThat(messages.sorted(), equalTo(listOf("probation-case.registration.added", "probation-case.risk-scores.ogrs.manual-calculation")))
        verify(telemetryService, timeout(30000)).trackEvent(eq("DomainEventsProcessed"), any(), any())
        assertEquals(0, domainEventRepository.count())
    }
}
