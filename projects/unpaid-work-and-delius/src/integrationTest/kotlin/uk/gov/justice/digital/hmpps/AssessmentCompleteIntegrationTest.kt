package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.data.generator.CaseGenerator
import uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.document.DocumentRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
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

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `complete an UPW assessment`() {
        val notification = prepMessage("upw-assessment-complete", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val contacts = contactRepository.findAll().filter { it.person.crn == CaseGenerator.DEFAULT.crn }
        MatcherAssert.assertThat(contacts.size, Matchers.equalTo(1))
        MatcherAssert.assertThat(contacts[0].notes, Matchers.equalTo("CP/UPW Assessment"))
        MatcherAssert.assertThat(contacts[0].type.code, Matchers.equalTo(ContactTypeCode.UPW_ASSESSMENT.code))
        MatcherAssert.assertThat(contacts[0].person.crn, Matchers.equalTo(CaseGenerator.DEFAULT.crn))

        val documents = documentRepository.findAll()
        MatcherAssert.assertThat(documents.size, Matchers.equalTo(1))
        MatcherAssert.assertThat(documents[0].alfrescoId, Matchers.equalTo("alfresco-uuid"))
        MatcherAssert.assertThat(documents[0].name, Matchers.equalTo("upw.pdf"))
        MatcherAssert.assertThat(documents[0].contactId, Matchers.equalTo(contacts[0].id))
        MatcherAssert.assertThat(documents[0].tableName, Matchers.equalTo("CONTACT"))
        MatcherAssert.assertThat(documents[0].externalReference, Matchers.equalTo("12345"))

        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }
}
