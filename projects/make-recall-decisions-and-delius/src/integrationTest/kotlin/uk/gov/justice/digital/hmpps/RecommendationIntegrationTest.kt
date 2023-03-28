package uk.gov.justice.digital.hmpps

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
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactRepository
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@SpringBootTest
@ActiveProfiles("integration-test")
internal class RecommendationIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var contactRepository: ContactRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `when recommendation started - a contact is created`() {
        val message = MessageGenerator.RECOMMENDATION_STARTED
        val notification = Notification(message, MessageAttributes(message.eventType))
        channelManager.getChannel(queueName).publishAndWait(notification)

        val person = PersonGenerator.RECOMMENDATION_STARTED
        val contact = contactRepository.findAll().firstOrNull { it.personId == person.id }
        assertNotNull(contact!!)
        assertThat(contact.providerId, equalTo(PersonGenerator.DEFAULT_PROVIDER.id))
        assertThat(contact.teamId, equalTo(PersonGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(PersonGenerator.DEFAULT_STAFF.id))
        assertThat(contact.notes, equalTo("View details of this Recommendation: http://mrd.case.crn/overview"))
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }

    @Test
    fun `management overview decision to recall`() {
        val message = MessageGenerator.DECISION_TO_RECALL
        val notification = Notification(message, MessageAttributes(message.eventType))
        channelManager.getChannel(queueName).publishAndWait(notification)

        val person = PersonGenerator.DECISION_TO_RECALL
        val contact = contactRepository.findAll().firstOrNull { it.personId == person.id }
        assertNotNull(contact!!)
        assertThat(contact.providerId, equalTo(PersonGenerator.DEFAULT_PROVIDER.id))
        assertThat(contact.teamId, equalTo(PersonGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(StaffGenerator.DEFAULT.id))
        assertThat(contact.notes, equalTo("View details of the Manage a Recall Oversight Decision: http://mrd.case.crn/overview"))
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }

    @Test
    fun `management overview decision not to recall`() {
        val message = MessageGenerator.DECISION_NOT_TO_RECALL
        val notification = Notification(message, MessageAttributes(message.eventType))
        channelManager.getChannel(queueName).publishAndWait(notification)

        val person = PersonGenerator.DECISION_NOT_TO_RECALL
        val contact = contactRepository.findAll().firstOrNull { it.personId == person.id }
        assertNotNull(contact!!)
        assertThat(contact.providerId, equalTo(PersonGenerator.DEFAULT_PROVIDER.id))
        assertThat(contact.teamId, equalTo(PersonGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(StaffGenerator.DEFAULT.id))
        assertThat(contact.notes, equalTo("View details of the Manage a Recall Oversight Decision: http://mrd.case.crn/overview"))
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }
}
