package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.jms.convertSendAndWait
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}") lateinit var queueName: String
    @Autowired lateinit var embeddedActiveMQ: EmbeddedActiveMQ
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var wireMockServer: WireMockServer
    @Autowired lateinit var jmsTemplate: JmsTemplate
    @Autowired lateinit var contactRepository: ContactRepository
    @MockBean lateinit var telemetryService: TelemetryService

    @Test
    fun `approved premises key worker staff are returned successfully`() {
        val approvedPremises = ApprovedPremisesGenerator.DEFAULT
        mockMvc
            .perform(get("/approved-premises/${approvedPremises.code.code}/staff").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.numberOfElements", equalTo(2)))
            .andExpect(jsonPath("$.size", equalTo(100)))
            .andExpect(jsonPath("$.content[*].name.surname", equalTo(listOf("Key-worker", "Not key-worker"))))
            .andExpect(jsonPath("$.content[*].keyWorker", equalTo(listOf(true, false))))
    }

    @Test
    fun `empty approved premises returns 200 with empty results`() {
        val approvedPremises = ApprovedPremisesGenerator.NO_STAFF
        mockMvc
            .perform(get("/approved-premises/${approvedPremises.code.code}/staff").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.numberOfElements", equalTo(0)))
            .andExpect(jsonPath("$.totalElements", equalTo(0)))
    }

    @Test
    fun `non-existent approved premises returns 404`() {
        mockMvc
            .perform(get("/approved-premises/NOTFOUND/staff").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", equalTo("Approved Premises with code of NOTFOUND not found")))
    }

    @Test
    fun `approved premises key workers only are returned successfully`() {
        val approvedPremises = ApprovedPremisesGenerator.DEFAULT
        mockMvc
            .perform(get("/approved-premises/${approvedPremises.code.code}/staff?keyWorker=true").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.numberOfElements", equalTo(1)))
            .andExpect(jsonPath("$.content[*].name.surname", equalTo(listOf("Key-worker"))))
            .andExpect(jsonPath("$.content[*].keyWorker", equalTo(listOf(true))))
    }

    @Test
    fun `application submission creates an alert contact`() {
        // Given an application-submitted event
        val event = prepEvent("application-submitted", wireMockServer.port())

        // When it is received
        jmsTemplate.convertSendAndWait(embeddedActiveMQ, queueName, event)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(event)
        verify(telemetryService).trackEvent("ApplicationSubmitted", event.message.telemetryProperties())

        // And a contact alert is created
        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.personReference.findCrn() && it.type.code == ContactTypeCode.APPLICATION_SUBMITTED.code }
        assertThat(contact.alert, equalTo(true))
    }

    @Test
    fun `application assessed creates an alert contact`() {
        // Given an application-assessed event
        val event = prepEvent("application-assessed", wireMockServer.port())

        // When it is received
        jmsTemplate.convertSendAndWait(embeddedActiveMQ, queueName, event)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(event)
        verify(telemetryService).trackEvent("ApplicationAssessed", event.message.telemetryProperties())

        // And a contact alert is created
        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.personReference.findCrn() && it.type.code == ContactTypeCode.APPLICATION_ASSESSED.code }
        assertThat(contact.alert, equalTo(true))
        assertThat(contact.description, equalTo("Approved Premises Application Rejected"))
        assertThat(contact.notes, equalTo("Risk too low"))
    }
}
