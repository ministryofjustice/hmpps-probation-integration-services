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
class StatusChangedEventIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}") private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val wireMockServer: WireMockServer,
    private val contactRepository: ContactRepository,
) {

    @Test
    fun `breach status contact created`() {
        val eventName = "status-changed-breach"
        val event = prepMessage(eventName, wireMockServer.port())


        channelManager.getChannel(queueName).publishAndWait(
            event.copy(
                message = event.message.copy(
                    detailUrl = event.message.detailUrl?.replace(":id", "${TestData.LICENCE_CONDITIONS.first().id}")
                )
            )
        )

        val contact = contactRepository.findAll().firstOrNull {
            it.person.id == TestData.PERSON.id && it.type.code == StatusInfo.Status.BREACH.contactTypeCode
        }
        assertThat(contact).isNotNull
        assertThat(contact!!.licenceCondition?.id).isEqualTo(TestData.LICENCE_CONDITIONS.first().id)
        assertThat(contact.notes).isEqualTo("Some notes about the breach of the LC")
        assertThat(contact.externalReference).isNotNull
    }

    @Test
    fun `on programme status contact created`() {
        val eventName = "status-changed-on-programme"
        val event = prepMessage(eventName, wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(
            event.copy(
                message = event.message.copy(
                    detailUrl = event.message.detailUrl?.replace(":id", "${TestData.REQUIREMENTS.first().id}")
                )
            )
        )

        val contact = contactRepository.findAll().firstOrNull {
            it.person.id == TestData.PERSON.id && it.type.code == StatusInfo.Status.ON_PROGRAMME.contactTypeCode
        }
        assertThat(contact).isNotNull
        assertThat(contact!!.requirement?.id).isEqualTo(TestData.REQUIREMENTS.first().id)
        assertThat(contact.notes).isEqualTo("Some notes about the being on the programme")
        assertThat(contact.externalReference).isNotNull
    }
}