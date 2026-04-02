package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.api.model.contact.CreateContact
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived

@SpringBootTest
internal class MessagingIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var personRepository: PersonRepository

    @Test
    fun `sms contact is created`() {
        // Given a message
        val notification = prepEvent("sms-to-pop", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(notification)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(notification)
        verify(telemetryService).trackEvent(
            "SmsContactCreated",
            mapOf(
                "crn" to "X012771",
                "id" to "00000000-0000-0000-0000-000000000001"
            ),
            mapOf()
        )

        val pop = personRepository.findByCrn("X012771")
        val contacts = contactRepository.findByPersonId(pop!!.id)
        val actualContact = contacts.first { it.type.code == CreateContact.Type.EmailTextToPoP.code }
        assertThat(actualContact.notes).startsWith("This is a test message about your appointment")
    }
}