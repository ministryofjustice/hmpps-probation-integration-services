package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
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
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Value("\${messaging.producer.topic}")
    lateinit var topicName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var hmppsChannelManager: HmppsChannelManager

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

    @BeforeEach
    fun setup() {
        doReturn("A111111").whenever(personService).generateCrn()
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
        thenNoRecordsAreInserted()
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
        thenNoRecordsAreInserted()
    }

    @Test
    fun `When a message without a judicial result of remanded in custody is found`() {
        wireMockServer.stubFor(
            post(urlPathEqualTo("/probation-search/match"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("probation-search-no-results.json")
                )
        )

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NO_REMAND)
        channelManager.getChannel(queueName).publishAndWait(notification)
        thenNoRecordsAreInserted()
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

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_DOB_ERROR)
        channelManager.getChannel(queueName).publishAndWait(notification)
        thenNoRecordsAreInserted()
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
    fun `When a hearing message with missing required fields is detected no records are inserted`() {
        wireMockServer.stubFor(
            post(urlPathEqualTo("/probation-search/match"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("probation-search-no-results.json")
                )
        )

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NULL_FIELDS)
        channelManager.getChannel(queueName).publishAndWait(notification)
        thenNoRecordsAreInserted()
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

        verify(personService).insertAddress(any())
        verify(personService).findAddressByFreeText(any())

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

        verify(personService, never()).insertAddress(any())

        verify(addressRepository, never()).save(any())

        verify(auditedInteractionService, never()).createAuditedInteraction(
            eq(BusinessInteractionCode.INSERT_ADDRESS),
            any(),
            eq(AuditedInteraction.Outcome.SUCCESS),
            any(),
            anyOrNull()
        )
    }

    @Order(1)
    @Test
    fun `engagement created and address created sns messages are published on insert person`() {
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

        val topic = hmppsChannelManager.getChannel(topicName)
        val messages = topic.pollFor(2)
        val messageTypes = messages.mapNotNull { it.eventType }

        assertThat(
            messageTypes.sorted(),
            Matchers.equalTo(
                listOf(
                    "probation-case.address.created",
                    "probation-case.engagement.created"
                )
            )
        )

        messages.forEach { message ->
            val event = message.message as HmppsDomainEvent

            when (event.eventType) {
                "probation-case.engagement.created" -> {
                    assertEquals(1, event.version)
                    assertEquals("probation-case.engagement.created", event.eventType)
                    assertEquals("A probation case record for a person has been created in Delius", event.description)
                    assertNotNull(event.personReference.findCrn())
                    assertNull(event.detailUrl)
                    assertTrue(event.additionalInformation.isEmpty())
                }

                "probation-case.address.created" -> {
                    assertEquals(1, event.version)
                    assertEquals("probation-case.address.created", event.eventType)
                    assertEquals("A new address has been created on the probation case", event.description)
                    assertNotNull(event.personReference.findCrn())
                    assertNull(event.detailUrl)

                    with(event.additionalInformation) {
                        assertTrue(containsKey("addressStatus"))
                        assertTrue(containsKey("addressId"))
                    }
                }

                else -> fail("Unexpected event type: ${event.eventType}")
            }
        }
    }


    @Test
    fun `court hearing address inserted no address lookup api result is found`() {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/address-lookup/search/places/v1/find"))
                .willReturn(
                    okJson(
                        """
                {
                    "header": { "totalresults": 0 },
                    "results": []
                }
            """
                    )
                )
        )
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)

        val addressCaptor = argumentCaptor<PersonAddress>()
        verify(addressRepository).save(addressCaptor.capture())

        val savedAddress = addressCaptor.firstValue
        assertThat(savedAddress.notes, Matchers.containsString("Example Address Line 1"))
        assertThat(savedAddress.postcode, Matchers.containsString("AA1 1AA"))
    }

    @Test
    fun `Address lookup api is inserted when result is found`() {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/address-lookup/search/places/v1/find"))
                .willReturn(
                    okJson(
                        """
                {
                    "header": { "totalresults": 1 },
                    "results": [
                        {
                            "DPA": {
                                "address": "123 Test Street, Test, AB1 2CD",
                                "postcode": "AB1 2CD",
                                "buildingNumber": "123",
                                "thoroughfareName": "Test Street",
                                "postTown": "Test",
                                "match": 0.9
                            }
                        }
                    ]
                }
                """
                    )
                )
        )

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)

        val addressCaptor = argumentCaptor<PersonAddress>()
        verify(addressRepository).save(addressCaptor.capture())

        val savedAddress = addressCaptor.firstValue
        assertThat(savedAddress.notes, Matchers.containsString("123 Test Street, Test, AB1 2CD"))
        assertThat(savedAddress.postcode, Matchers.containsString("AB1 2CD"))
        assertThat(savedAddress.streetName, Matchers.containsString("Test Street"))
        assertThat(savedAddress.addressNumber, Matchers.containsString("123"))
        assertThat(savedAddress.town, Matchers.containsString("Test"))
    }

    private fun thenNoRecordsAreInserted() {
        verify(personService, never()).insertAddress(any())
        verify(addressRepository, never()).save(any())
        verify(personRepository, never()).save(any())
        verify(auditedInteractionService, Mockito.never())
            .createAuditedInteraction(any(), any(), eq(AuditedInteraction.Outcome.SUCCESS), any(), anyOrNull())
    }
}