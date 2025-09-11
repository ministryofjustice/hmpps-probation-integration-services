package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.*
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch.core.IndexRequest
import org.opensearch.client.util.ObjectBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generate
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.client.CorePersonClient
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.CommonPlatformHearing
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.service.EventService
import uk.gov.justice.digital.hmpps.service.OffenceService
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.function.Function

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    private lateinit var referenceDataRepository: ReferenceDataRepository

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

    @MockitoSpyBean
    lateinit var eventRepository: EventRepository

    @MockitoSpyBean
    lateinit var courtAppearanceRepository: CourtAppearanceRepository

    @MockitoSpyBean
    lateinit var contactRepository: ContactRepository

    @MockitoSpyBean
    lateinit var courtRepository: CourtRepository

    @MockitoSpyBean
    lateinit var mainOffenceRepository: MainOffenceRepository

    @MockitoSpyBean
    lateinit var additionalOffenceRepository: AdditionalOffenceRepository

    @MockitoSpyBean
    lateinit var orderManagerRepository: OrderManagerRepository

    @MockitoSpyBean
    lateinit var eventService: EventService

    @MockitoBean
    lateinit var openSearchClient: OpenSearchClient

    @MockitoSpyBean
    lateinit var offenceService: OffenceService

    @MockitoBean
    lateinit var s3Client: S3Client

    @MockitoSpyBean
    lateinit var corePersonClient: CorePersonClient

    @BeforeEach
    fun setup() {
        doReturn("A111111").whenever(personService).generateCrn()
        whenever(featureFlags.enabled("common-platform-record-creation-toggle")).thenReturn(true)
    }

    @Test
    fun `Message is logged to telemetry and opensearch`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)
        verify(openSearchClient).index(any<Function<IndexRequest.Builder<CommonPlatformHearing>, ObjectBuilder<IndexRequest<CommonPlatformHearing>>>>())
    }

    @Order(2)
    @Test
    fun `When a probation search match is detected no insert is performed`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_WITH_CRN)
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

        verify(personRepository, times(2)).save(check<Person> {
            assertThat(it.forename, Matchers.equalTo("Example First Name"))
            assertThat(it.surname, Matchers.equalTo("Example Last Name"))
            assertThat(it.mobileNumber, Matchers.equalTo("07000000000"))
            assertThat(it.telephoneNumber, Matchers.equalTo("01234567890"))
        })

        verify(auditedInteractionService).createAuditedInteraction(
            eq(BusinessInteractionCode.INSERT_PERSON),
            check {
                assertThat(it.paramPairs().size, Matchers.equalTo(2))
                assertThat(it["offenderId"], Matchers.notNullValue())
                assertThat(it["crn"], Matchers.equalTo("A111111"))
            },
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
            Assertions.assertNull(it.endDate)
            Assertions.assertNotNull(it.notes)
            assertThat(it.softDeleted, Matchers.equalTo(false))
            assertThat(it.status.code, Matchers.equalTo(ReferenceData.StandardRefDataCode.ADDRESS_MAIN_STATUS.code))
            assertThat(it.noFixedAbode, Matchers.equalTo(false))
            assertThat(it.type.code, Matchers.equalTo(ReferenceData.StandardRefDataCode.AWAITING_ASSESSMENT.code))
            assertThat(it.typeVerified, Matchers.equalTo(false))
        })

        verify(auditedInteractionService).createAuditedInteraction(
            eq(BusinessInteractionCode.INSERT_ADDRESS),
            check {
                assertThat(it.paramPairs().size, Matchers.equalTo(1))
                assertThat(it["offenderId"], Matchers.notNullValue())
            },
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
        mockS3Client()

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(personService).insertPerson(any(), any())

        verify(personRepository, times(2)).save(check<Person> {
            assertThat(it.forename, Matchers.equalTo("Example First Name"))
            assertThat(it.surname, Matchers.equalTo("Example Last Name"))
            assertThat(it.mobileNumber, Matchers.equalTo("07000000000"))
            assertThat(it.telephoneNumber, Matchers.equalTo("01234567890"))
        })

        verify(addressRepository).save(check<PersonAddress> {
            assertThat(it.start, Matchers.equalTo(LocalDate.now()))
            Assertions.assertNull(it.endDate)
            Assertions.assertNotNull(it.notes)
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
                    Assertions.assertNotNull(event.personReference.findCrn())
                    assertEquals(
                        "http://domain-events-and-delius.test.gov.uk/probation-case.engagement.created/${event.personReference.findCrn()}",
                        event.detailUrl
                    )
                    assertTrue(event.additionalInformation.isEmpty())
                }

                "probation-case.address.created" -> {
                    assertEquals(1, event.version)
                    assertEquals("probation-case.address.created", event.eventType)
                    assertEquals("A new address has been created on the probation case", event.description)
                    Assertions.assertNotNull(event.personReference.findCrn())
                    Assertions.assertNull(event.detailUrl)

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
            Assertions.assertNull(it.endDate)
            assertThat(
                it.notes,
                Matchers.equalTo("This address record was initially created using information from HMCTS Common Platform.")
            )
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
            Assertions.assertNull(it.endDate)
            Assertions.assertNotNull(it.notes)
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
        mockS3Client()
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(eventService).insertEvent(any(), any(), any(), any(), any(), any(), any())

        verify(eventRepository).save(check<Event> {
            assertThat(it.person.forename, Matchers.equalTo("Example First Name"))
            assertThat(it.person.surname, Matchers.equalTo("Example Last Name"))
            assertThat(it.referralDate, Matchers.equalTo(LocalDate.of(2024, 1, 1)))
            assertTrue(it.active)
            assertThat(it.number, Matchers.equalTo("1"))
        })
        verify(mainOffenceRepository).save(check<MainOffence> {
            assertThat(it.person.forename, Matchers.equalTo("Example First Name"))
            assertThat(it.person.surname, Matchers.equalTo("Example Last Name"))
            assertThat(it.offence.description, Matchers.equalTo("Murder"))
            Assertions.assertNotNull(it.offence)
        })
        verify(additionalOffenceRepository).saveAll(check<List<AdditionalOffence>> {
            assertThat(it.first().offence.description, Matchers.equalTo("Second Offence"))
        })
        verify(courtAppearanceRepository).save(check<CourtAppearance> {
            assertThat(it.person.forename, Matchers.equalTo("Example First Name"))
            assertThat(it.person.surname, Matchers.equalTo("Example Last Name"))
            assertThat(
                it.appearanceType.code,
                Matchers.equalTo(ReferenceData.StandardRefDataCode.TRIAL_ADJOURNMENT_APPEARANCE.code)
            )
            assertThat(it.appearanceDate, Matchers.equalTo(LocalDateTime.of(2024, 1, 1, 12, 0)))

        })
        verify(contactRepository).save(check<Contact> {
            assertThat(it.person.forename, Matchers.equalTo("Example First Name"))
            assertThat(it.person.surname, Matchers.equalTo("Example Last Name"))
            assertThat(it.type.code, Matchers.equalTo(ContactTypeCode.COURT_APPEARANCE.code))
        })
        verify(orderManagerRepository).save(check<OrderManager> {
            assertThat(it.event.person.forename, Matchers.equalTo("Example First Name"))
            assertThat(it.event.person.surname, Matchers.equalTo("Example Last Name"))
            assertThat(it.allocationReason.description, Matchers.equalTo("Initial Allocation"))
            assertTrue(it.active)
            assertThat(it.allocationDate, Matchers.equalTo(LocalDate.of(2024, 1, 1)))
            Assertions.assertNull(it.endDate)
        })

        verify(personService).updatePerson(check<Person> {
            assertThat(it.remandStatus, Matchers.equalTo(referenceDataRepository.remandedInCustodyStatus().description))
        })

        verify(auditedInteractionService).createAuditedInteraction(
            eq(BusinessInteractionCode.INSERT_EVENT),
            check {
                assertThat(it.paramPairs().size, Matchers.equalTo(1))
                assertThat(it["offenderId"], Matchers.notNullValue())
            },
            eq(AuditedInteraction.Outcome.SUCCESS),
            any(),
            anyOrNull()
        )
    }

    @Test
    fun `When an error occurs during insertPerson, the created person record is rolled back`() {
        whenever(courtRepository.findByOuCode(anyString())).thenThrow(IllegalArgumentException("Court not found"))
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(personRepository).save(any())
        assertEquals(personRepository.count(), 0)
    }

    @Test
    fun `Main offence is the offence with the lowest priority when message contains multiple offences`() {
        mockS3Client()

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_MULTIPLE_OFFENCES)
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(mainOffenceRepository).save(check<MainOffence> {
            assertThat(it.offence.code, Matchers.equalTo("00101"))
        })
    }

    @Test
    fun `When a message is received without a slash separating the year in the PNC, a match can still be made`() {
        personRepository.save(
            generate(
                crn = "F019742",
                forename = "Example First Name",
                surname = "Example Last Name",
                dateOfBirth = LocalDate.of(2000, 1, 1),
                pnc = "2000/0000000A",
                cro = "000000/00A",
                id = null
            )
        )
        Mockito.reset(personRepository)

        // Message with PNC without a slash (e.g., "2000000000A")
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_NO_PNC_SLASH)
        channelManager.getChannel(queueName).publishAndWait(notification)

        // Match found so no record will be inserted
        thenNoRecordsAreInserted()
    }

    @Test
    fun `No insert is attempted if person record with same defendant id already exists`() {
        personRepository.save(
            generate(
                crn = "F019742",
                forename = "Example First Name",
                surname = "Example Last Name",
                dateOfBirth = LocalDate.of(2000, 1, 1),
                pnc = "2025/0123456A",
                cro = "123456/99A",
                id = null,
                defendantId = "f3b3bdb3-10c4-48fe-a412-9924f47294d5"
            )
        )
        Mockito.reset(personRepository)

        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)
        thenNoRecordsAreInserted()
    }

    @Test
    fun `Defendant and CRN is submitted to CPR after creation`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT)
        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(corePersonClient).createPersonRecord(eq("f3b3bdb3-10c4-48fe-a412-9924f47294d5"), any())

        verify(telemetryService, atLeastOnce()).trackEvent(
            "CPRRecordCreated",
            mapOf(
                "hearingId" to "00000000-0000-0000-0000-000000000000",
                "defendantId" to "f3b3bdb3-10c4-48fe-a412-9924f47294d5",
                "CRN" to "A111111"
            )
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

    private fun mockS3Client() {
        val mockCsv = """
            ho_offence_code,offence_desc,priority,offence_type,max_custodial_sentence
            00100,Test offence 1,20,Type,30
            00101,Test offence 2,60,Type,240
            00102,Test offence 3,30,Type,100
        """.trimIndent()
        val inputStream = ByteArrayInputStream(mockCsv.toByteArray())
        val responseStream = ResponseInputStream(GetObjectResponse.builder().build(), inputStream)
        val request = GetObjectRequest.builder()
            .bucket("offence-priority-bucket")
            .key("offence_priority.csv")
            .build()

        whenever(s3Client.getObject(request)).thenReturn(responseStream)
    }

    @AfterEach
    fun resetWireMock() {
        wireMockServer.resetAll()
    }

    @AfterEach
    fun cleanup() {
        courtAppearanceRepository.deleteAll()
        additionalOffenceRepository.deleteAll()
        mainOffenceRepository.deleteAll()
        orderManagerRepository.deleteAll()
        eventRepository.deleteAll()
        addressRepository.deleteAll()
        contactRepository.deleteAll()
        personManagerRepository.deleteAll()
        personRepository.deleteAll()
    }
}