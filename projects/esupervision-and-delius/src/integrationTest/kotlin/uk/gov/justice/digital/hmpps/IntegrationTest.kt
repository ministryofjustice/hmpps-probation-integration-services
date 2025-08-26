package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.ContactType
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.description
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Test
    fun `esupervision received contact created`() {
        val message = MessageGenerator.RECEIVED_A000001
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.person.id == PersonGenerator.DEFAULT_PERSON.id && it.description == "E Supervision Check-In Completed"
        }
        assertThat(contact.type.code).isEqualTo(ContactType.E_SUPERVISION_CHECK_IN)
        assertThat(contact.date).isEqualTo(notification.message.occurredAt.toLocalDate())
        assertThat(contact.event.id).isEqualTo(PersonGenerator.DEFAULT_EVENT.id)
        assertThat(contact.provider.id).isEqualTo(ProviderGenerator.DEFAULT_PROVIDER.id)
        assertThat(contact.team.id).isEqualTo(ProviderGenerator.DEFAULT_TEAM.id)
        assertThat(contact.staff.id).isEqualTo(ProviderGenerator.DEFAULT_STAFF.id)
        assertThat(contact.alert).isEqualTo(true)
        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo("For more information please visit https://esupervision/check-in/received")
    }

    @Test
    fun `esupervision expired contact created`() {
        val message = MessageGenerator.EXPIRED_A000001
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))
        channelManager.getChannel(queueName).publishAndWait(notification)

        val contact = contactRepository.findAll().single {
            it.person.id == PersonGenerator.DEFAULT_PERSON.id && it.description == "E Supervision 72 hours lapsed"
        }
        assertThat(contact.type.code).isEqualTo(ContactType.E_SUPERVISION_CHECK_IN)
        assertThat(contact.date).isEqualTo(notification.message.occurredAt.toLocalDate())
        assertThat(contact.event.id).isEqualTo(PersonGenerator.DEFAULT_EVENT.id)
        assertThat(contact.provider.id).isEqualTo(ProviderGenerator.DEFAULT_PROVIDER.id)
        assertThat(contact.team.id).isEqualTo(ProviderGenerator.DEFAULT_TEAM.id)
        assertThat(contact.staff.id).isEqualTo(ProviderGenerator.DEFAULT_STAFF.id)
        assertThat(contact.alert).isEqualTo(true)
        assertThat(contact.isSensitive).isEqualTo(false)
        assertThat(contact.notes).isEqualTo("For more information please visit https://esupervision/check-in/expired")
    }
}
