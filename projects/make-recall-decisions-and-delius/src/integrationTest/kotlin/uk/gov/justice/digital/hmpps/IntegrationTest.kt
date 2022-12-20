package uk.gov.justice.digital.hmpps

import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactRepository
import uk.gov.justice.digital.hmpps.jms.convertSendAndWait
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@SpringBootTest
@ActiveProfiles("integration-test")
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String
    @Autowired
    lateinit var embeddedActiveMQ: EmbeddedActiveMQ
    @Autowired
    lateinit var jmsTemplate: JmsTemplate
    @Autowired
    lateinit var contactRepository: ContactRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `when recommendation started - a contact is created`() {
        val message = MessageGenerator.RECOMMENDATION_STARTED
        val notification = Notification(message, MessageAttributes(message.eventType))
        jmsTemplate.convertSendAndWait(embeddedActiveMQ, queueName, notification)

        val person = PersonGenerator.RECOMMENDATION_STARTED
        val contact = contactRepository.findAll().firstOrNull { it.personId == person.id }
        assertNotNull(contact!!)
        assertThat(contact.providerId, equalTo(1))
        assertThat(contact.teamId, equalTo(2))
        assertThat(contact.staffId, equalTo(3))
        assertThat(contact.notes, equalTo("View details of this Recommendation: http://mrd.case.crn/overview"))
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }
}
