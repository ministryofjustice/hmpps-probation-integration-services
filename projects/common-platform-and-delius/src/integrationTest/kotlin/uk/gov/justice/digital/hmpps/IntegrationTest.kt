package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.service.AddressService
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockBean
    lateinit var telemetryService: TelemetryService

    @SpyBean
    lateinit var auditedInteractionService: AuditedInteractionService

    @SpyBean
    lateinit var personRepository: PersonRepository

    @SpyBean
    lateinit var addressRepository: PersonAddressRepository

    @SpyBean
    lateinit var personService: PersonService

    @SpyBean
    lateinit var addressService: AddressService

    @BeforeEach
    fun setup() {
        doReturn("A000001").whenever(personService).generateCrn()
    }

    @Test
    fun `Message is logged to telemetry`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
    }

    @Test
    fun `When a probation search match is detected no insert is performed`() {
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
        verify(addressService, never()).insertAddress(any())
        verify(addressRepository, never()).save(any())
        verify(auditedInteractionService, Mockito.never()).createAuditedInteraction(
            any(),
            any(),
            any(),
            any(),
            anyOrNull()
        )
    }

    @Test
    fun `When a message with no prosecution cases is found no insert is performed`() {
        wireMockServer.stubFor(
            post(urlPathEqualTo("/probation-search/match"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("probation-search-no-results.json")
                )
        )

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NO_CASES)

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(personService, never()).insertPerson(any(), any())
        verify(addressService, never()).insertAddress(any())
        verify(addressRepository, never()).save(any())
        verify(personRepository, never()).save(any())
        verify(auditedInteractionService, Mockito.never())
            .createAuditedInteraction(any(), any(), any(), any(), anyOrNull())
    }

    @Test
    fun `When a person under 10 years old is found no insert is performed`() {
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

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(personService, never()).insertPerson(any(), any())
        verify(personRepository, never()).save(any())
        verify(addressService, never()).insertAddress(any())
        verify(addressRepository, never()).save(any())
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
            assertThat(it.mobileNumber, Matchers.equalTo("07000000000"))
            assertThat(it.telephoneNumber, Matchers.equalTo("01234567890"))
        })

        verify(auditedInteractionService).createAuditedInteraction(
            eq(BusinessInteractionCode.INSERT_PERSON),
            any(),
            eq(AuditedInteraction.Outcome.SUCCESS),
            any(),
            anyOrNull()
        )
    }

    @Test
    fun `When a hearing with an address is received then an address record is inserted`() {
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

        verify(addressService).insertAddress(any())

        verify(addressRepository).save(check<PersonAddress> {
            assertThat(it.start, Matchers.equalTo(LocalDate.now()))
            assertNull(it.endDate)
            assertNotNull(it.notes)
            assertThat(it.softDeleted, Matchers.equalTo(false))
            assertThat(it.status.code, Matchers.equalTo(ReferenceData.StandardRefDataCode.ADDRESS_MAIN_STATUS.code))
            assertThat(it.noFixedAbode, Matchers.equalTo(false))
            assertThat(it.type.code, Matchers.equalTo(ReferenceData.StandardRefDataCode.AWAITING_ASSESSMENT.code))
            assertThat(it.typeVerified, Matchers.equalTo(false))
        })

        verify(auditedInteractionService).createAuditedInteraction(
            eq(BusinessInteractionCode.INSERT_ADDRESS),
            any(),
            eq(AuditedInteraction.Outcome.SUCCESS),
            any(),
            anyOrNull()
        )
    }

    @Test
    fun `When a hearing with an empty address is received then an address record is not inserted`() {
        wireMockServer.stubFor(
            post(urlPathEqualTo("/probation-search/match"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("probation-search-no-results.json")
                )
        )

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_BLANK_ADDRESS)
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(addressService, never()).insertAddress(any())

        verify(addressRepository, never()).save(any())

        verify(auditedInteractionService, never()).createAuditedInteraction(
            eq(BusinessInteractionCode.INSERT_ADDRESS),
            any(),
            eq(AuditedInteraction.Outcome.SUCCESS),
            any(),
            anyOrNull()
        )
    }
}