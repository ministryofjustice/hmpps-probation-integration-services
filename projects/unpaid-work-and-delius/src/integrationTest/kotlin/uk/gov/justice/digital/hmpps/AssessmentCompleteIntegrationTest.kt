package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.data.generator.CaseGenerator
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.document.DocumentRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class AssessmentCompleteIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var documentRepository: DocumentRepository

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `complete an UPW assessment`() {
        val notification = prepEvent("upw-assessment-complete", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val contacts = contactRepository.findAll().filter { it.person.crn == CaseGenerator.DEFAULT.crn }
        assertThat(contacts.size, equalTo(1))
        assertThat(contacts[0].notes, equalTo("CP/UPW Assessment"))
        assertThat(contacts[0].type.code, equalTo(ContactTypeCode.UPW_ASSESSMENT.code))
        assertThat(contacts[0].person.crn, equalTo(CaseGenerator.DEFAULT.crn))
        assertThat(contacts[0].eventId, equalTo(1234567890))
        assertThat(contacts[0].sensitive, equalTo(false))
        assertThat(contacts[0].documentLinked, equalTo(true))
        assertThat(contacts[0].externalReference, equalTo("urn:hmpps:unpaid-work-assessment:12345"))

        val documents = documentRepository.findAll()
        assertThat(documents.size, equalTo(1))
        assertThat(documents[0].alfrescoId, equalTo("alfresco-uuid"))
        assertThat(documents[0].name, equalTo("David-Banner-${CaseGenerator.DEFAULT.crn}-UPW.pdf"))
        assertThat(documents[0].contactId, equalTo(contacts[0].id))
        assertThat(documents[0].tableName, equalTo("CONTACT"))
        assertThat(documents[0].externalReference, equalTo("urn:hmpps:unpaid-work-assessment:12345"))

        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }
}
