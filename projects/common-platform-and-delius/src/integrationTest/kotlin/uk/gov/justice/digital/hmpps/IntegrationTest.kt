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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.service.EventService
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

    @MockitoSpyBean
    lateinit var telemetryService: TelemetryService

    @MockitoSpyBean
    lateinit var auditedInteractionService: AuditedInteractionService

    @MockitoSpyBean
    lateinit var personRepository: PersonRepository

    @MockitoSpyBean
    lateinit var addressRepository: PersonAddressRepository

    @MockitoSpyBean
    lateinit var personManagerRepository: PersonManagerRepository

    @MockitoSpyBean
    lateinit var personService: PersonService

    @MockitoSpyBean
    private lateinit var featureFlags: FeatureFlags

    @SpyBean
    lateinit var eventRepository: EventRepository

    @SpyBean
    lateinit var courtAppearanceRepository: CourtAppearanceRepository

    @SpyBean
    lateinit var contactRepository: ContactRepository

    @SpyBean
    lateinit var mainOffenceRepository: MainOffenceRepository

    @SpyBean
    lateinit var orderManagerRepository: OrderManagerRepository

    @SpyBean
    lateinit var eventService: EventService

    @BeforeEach
    fun setup() {
        doReturn("A111111").whenever(personService).generateCrn()
        whenever(featureFlags.enabled("common-platform-record-creation-toggle")).thenReturn(true)
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
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NO_CASES)
        channelManager.getChannel(queueName).publishAndWait(notification)
        thenNoRecordsAreInserted()
    }

    @Test
    fun `When a message without a judicial result of remanded in custody is found`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NO_REMAND)
        channelManager.getChannel(queueName).publishAndWait(notification)
        thenNoRecordsAreInserted()
    }

    @Test
    fun `When a person under 10 years old is found no insert is performed`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_DOB_ERROR)
        channelManager.getChannel(queueName).publishAndWait(notification)
        thenNoRecordsAreInserted()
    }

    @Test
    fun `When a probation search match is not detected then a person is inserted`() {
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
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NULL_FIELDS)
        channelManager.getChannel(queueName).publishAndWait(notification)
        thenNoRecordsAreInserted()
    }

    @Test
    fun `When a hearing with an address is received then an address record is inserted`() {
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
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(personService).insertPerson(any(), any())

        verify(personRepository).save(check<Person> {
            assertThat(it.forename, Matchers.equalTo("Example First Name"))
            assertThat(it.surname, Matchers.equalTo("Example Last Name"))
            assertThat(it.mobileNumber, Matchers.equalTo("07000000000"))
            assertThat(it.telephoneNumber, Matchers.equalTo("01234567890"))
        })

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
    fun `court hearing address is inserted when no address lookup is found`() {
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

        verify(addressRepository).save(check<PersonAddress> {
            assertThat(it.start, Matchers.equalTo(LocalDate.now()))
            assertThat(it.streetName, Matchers.containsString("Example Address Line 1"))
            assertThat(it.district, Matchers.containsString("Example Address Line 2"))
            assertThat(it.town, Matchers.containsString("Example Address Line 3"))
            assertThat(it.postcode, Matchers.containsString("AA1 1AA"))
            assertNull(it.endDate)
            assertNull(it.notes)
            assertThat(it.softDeleted, Matchers.equalTo(false))
            assertThat(it.status.code, Matchers.equalTo(ReferenceData.StandardRefDataCode.ADDRESS_MAIN_STATUS.code))
            assertThat(it.noFixedAbode, Matchers.equalTo(false))
            assertThat(it.type.code, Matchers.equalTo(ReferenceData.StandardRefDataCode.AWAITING_ASSESSMENT.code))
            assertThat(it.typeVerified, Matchers.equalTo(false))
        })
    }

    @Test
    fun `Address lookup api is inserted when result is found`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(addressRepository).save(check<PersonAddress> {
            assertThat(it.start, Matchers.equalTo(LocalDate.now()))
            assertThat(it.notes, Matchers.containsString("UPRN: 123456789012"))
            assertThat(it.postcode, Matchers.containsString("AB1 2CD"))
            assertThat(it.streetName, Matchers.containsString("Test Street"))
            assertThat(it.addressNumber, Matchers.containsString("123"))
            assertThat(it.town, Matchers.containsString("Test"))
            assertNull(it.endDate)
            assertNotNull(it.notes)
            assertThat(it.softDeleted, Matchers.equalTo(false))
            assertThat(it.status.code, Matchers.equalTo(ReferenceData.StandardRefDataCode.ADDRESS_MAIN_STATUS.code))
            assertThat(it.noFixedAbode, Matchers.equalTo(false))
            assertThat(it.type.code, Matchers.equalTo(ReferenceData.StandardRefDataCode.AWAITING_ASSESSMENT.code))
            assertThat(it.typeVerified, Matchers.equalTo(false))
        })
    }

    @Test
    fun `When feature flag is disabled no records are inserted`() {
        whenever(featureFlags.enabled("common-platform-record-creation-toggle")).thenReturn(false)
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(personService, never()).insertPerson(any(), any())
        thenNoRecordsAreInserted()
        verify(auditedInteractionService, Mockito.never())
            .createAuditedInteraction(any(), any(), eq(AuditedInteraction.Outcome.FAIL), any(), anyOrNull())
    }

    @Test
    fun `A hearing message with a remanded offence is received and an event is inserted`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(eventService).insertEvent(any(), any(), any(), any(), any(), any())
        verify(eventService, never()).insertCourtAppearance(any(), any(), any(), any(), any())

        verify(eventRepository).save(check<Event> {
            assertThat(it.person.forename, Matchers.equalTo("Example First Name"))
            assertThat(it.person.surname, Matchers.equalTo("Example Last Name"))
            assertThat(it.referralDate, Matchers.equalTo(LocalDate.of(2024, 1, 1)))
            assertTrue(it.active)
            assertThat(it.number, Matchers.equalTo("1"))
        })

        verify(auditedInteractionService).createAuditedInteraction(
            eq(BusinessInteractionCode.INSERT_EVENT),
            any(),
            eq(AuditedInteraction.Outcome.SUCCESS),
            any(),
            anyOrNull()
        )
    }

    private fun thenNoRecordsAreInserted() {
        verify(orderManagerRepository, never()).save(any())
        verify(courtAppearanceRepository, never()).save(any())
        verify(contactRepository, never()).save(any())
        verify(eventRepository, never()).save(any())
        verify(mainOffenceRepository, never()).save(any())
        verify(addressRepository, never()).save(any())
        verify(personRepository, never()).save(any())
        verify(personManagerRepository, never()).save(any())
        verify(auditedInteractionService, Mockito.never())
            .createAuditedInteraction(any(), any(), eq(AuditedInteraction.Outcome.SUCCESS), any(), anyOrNull())
    }

    @AfterEach
    fun resetWireMock() {
        wireMockServer.resetAll()
    }

    @AfterEach
    fun cleanup() {
        courtAppearanceRepository.deleteAll()
        mainOffenceRepository.deleteAll()
        orderManagerRepository.deleteAll()
        eventRepository.deleteAll()
        addressRepository.deleteAll()
        contactRepository.deleteAll()
        personManagerRepository.deleteAll()
        personRepository.deleteAll()
    }
}