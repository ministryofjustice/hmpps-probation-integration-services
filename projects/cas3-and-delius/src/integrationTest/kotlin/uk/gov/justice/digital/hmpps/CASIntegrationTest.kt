package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.util.concurrent.TimeoutException

@SpringBootTest
internal class CASIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired lateinit var channelManager: HmppsChannelManager

    @MockBean lateinit var telemetryService: TelemetryService

    @Autowired lateinit var contactRepository: ContactRepository

    @Test
    fun `message is processed correctly`() {
        // Given a message
        val message = prepMessage(MessageGenerator.REFERRAL_SUBMITTED).message
        val notification = Notification(message = message)

        // When it is received
        try {
            channelManager.getChannel(queueName).publishAndWait(notification)
        } catch (_: TimeoutException) {
            // Note: Remove this try/catch when the MessageListener logic has been implemented
        }

        // Then it is logged to telemetry
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)

        // verify that the contact has been created:

        val contact = contactRepository.getByExternalReference(message.additionalInformation["applicationId"] as String)

        MatcherAssert.assertThat(contact!!.type.code, Matchers.equalTo("EARS"))
    }
}
