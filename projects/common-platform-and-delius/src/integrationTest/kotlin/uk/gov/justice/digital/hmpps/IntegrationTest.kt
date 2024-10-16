package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.repository.CourtRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.repository.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.repository.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @MockBean
    lateinit var telemetryService: TelemetryService

    @MockBean
    lateinit var referenceDataRepository: ReferenceDataRepository

    @MockBean
    lateinit var courtRepository: CourtRepository

    @SpyBean
    lateinit var personRepository: PersonRepository

    @SpyBean
    lateinit var personService: PersonService

    @Test
    fun `Message is logged to telemetry`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }

    @Test
    fun `When a probation search match is detected then a person is not inserted`() {
        wireMockServer.stubFor(
            post(urlPathEqualTo("/probation-search/match"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("probation-search-single-result.json")
                )
        )

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(personService, never()).insertPerson(any(), any())
        verify(personRepository, never()).save(any())
        verify(auditedInteractionService, Mockito.never()).createAuditedInteraction(
            any(),
            any(),
            any(),
            any(),
            anyOrNull()
        )
    }

    @Test
    fun `When a person is 10 years old or under a person is not inserted`() {
        wireMockServer.stubFor(
            post(urlPathEqualTo("/probation-search/match"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("probation-search-no-results.json")
                )
        )

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_VALIDATION_ERROR)
        val exception = assertThrows<IllegalArgumentException> {
            channelManager.getChannel(queueName).publishAndWait(notification)
        }

        assert(exception.message!!.contains("Date of birth would indicate person is under ten years old"))

        verify(personService, never()).insertPerson(any(), any())
        verify(personRepository, never()).save(any())
        verify(auditedInteractionService, Mockito.never())
            .createAuditedInteraction(any(), any(), any(), any(), anyOrNull())
    }

    @Test
    fun `When a probation search match is not detected then a person is inserted`() {
        wireMockServer.stubFor(
            post(urlPathEqualTo("/probation-search/match"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("probation-search-no-results.json")
                )
        )

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(personService).insertPerson(any(), any())

        verify(personRepository).save(check<Person> {
            assertThat(it.forename, Matchers.equalTo("Example First Name"))
            assertThat(it.surname, Matchers.equalTo("Example Last Name"))
        })
        verify(auditedInteractionService).createAuditedInteraction(
            eq(BusinessInteractionCode.INSERT_PERSON),
            any(),
            any(),
            any(),
            anyOrNull()
        )
    }
}