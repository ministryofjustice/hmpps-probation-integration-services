package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class CASIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var contactRepository: ContactRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `referral submitted message is processed correctly`() {
        val event = prepEvent("referral-submitted", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val contact =
            contactRepository.getByExternalReference(event.message.additionalInformation["applicationId"] as String)

        MatcherAssert.assertThat(contact!!.type.code, Matchers.equalTo("EARS"))
    }

    @Test
    fun `booking cancelled message is processed correctly`() {
        val event = prepEvent("booking-cancelled", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val contact =
            contactRepository.getByExternalReference("14c80733-4b6d-4f35-b724-66955aac320c")

        MatcherAssert.assertThat(contact!!.type.code, Matchers.equalTo("EACA"))
    }

    @Test
    fun `booking confirmed message is processed correctly`() {
        val event = prepEvent("booking-confirmed", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val contact =
            contactRepository.getByExternalReference("14c80733-4b6d-4f35-b724-66955aac320c")

        MatcherAssert.assertThat(contact!!.type.code, Matchers.equalTo("EACO"))
    }
}
