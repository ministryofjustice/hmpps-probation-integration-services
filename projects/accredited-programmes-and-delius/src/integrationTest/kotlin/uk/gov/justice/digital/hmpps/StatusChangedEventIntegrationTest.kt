package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.integration.StatusInfo
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.repository.ContactRepository

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StatusChangedEventIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Test
    fun `breach status contact created`() {
        val eventName = "status-changed-breach"
        val event = prepMessage(eventName, wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(event)

        val contact = contactRepository.findAll().firstOrNull {
            it.person.id == TestData.PERSON.id && it.type.code == StatusInfo.Status.BREACH.contactTypeCode
        }
        assertThat(contact).isNotNull
        assertThat(contact?.licenceCondition?.id).isEqualTo(TestData.LICENCE_CONDITIONS.first().id)
    }

    @Test
    fun `on programme status contact created`() {
        val eventName = "status-changed-on-programme"
        val event = prepMessage(eventName, wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(event)

        val contact = contactRepository.findAll().firstOrNull {
            it.person.id == TestData.PERSON.id && it.type.code == StatusInfo.Status.ON_PROGRAMME.contactTypeCode
        }
        assertThat(contact).isNotNull
        assertThat(contact?.requirement?.id).isEqualTo(TestData.REQUIREMENTS.first().id)
    }
}