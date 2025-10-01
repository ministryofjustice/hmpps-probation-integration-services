package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.Mockito.verify
import org.mockito.kotlin.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator.PERSON_ADDRESS_ID
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.EventDetails
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonDeparted
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.SERVICE_URN_BASE
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.PreferredResidence
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.PreferredResidenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ReferralRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ResidenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.outcome.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.CASE_ALLOCATED
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ApprovedPremisesCategoryCode
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.service.EXT_REF_BOOKING_PREFIX
import uk.gov.justice.digital.hmpps.service.EXT_REF_PREARRIVAL_PREFIX
import uk.gov.justice.digital.hmpps.service.EXT_REF_REHABILITATIVE_ACTIVITY_PREFIX
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isSameTimeAs
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(OrderAnnotation::class)
internal class MessagingIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Value("\${messaging.producer.topic}")
    lateinit var topicName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var nsiRepository: NsiRepository

    @Autowired
    lateinit var personAddressRepository: PersonAddressRepository

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    private lateinit var referralRepository: ReferralRepository

    @Autowired
    private lateinit var residenceRepository: ResidenceRepository

    @Autowired
    private lateinit var preferredResidenceRepository: PreferredResidenceRepository

    @Autowired
    private lateinit var staffRepository: StaffRepository

    @Autowired
    private lateinit var nsiManagerRepository: NsiManagerRepository

    @BeforeEach
    fun clearTopic() {
        val topic = channelManager.getChannel(topicName)
        do {
            val message = topic.receive()?.also { topic.done(it.id) }
        } while (message != null)
    }

    @Test
    fun `application submission creates an alert contact`() {
        // Given an application-submitted event
        val event = prepEvent("application-submitted", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        verify(telemetryService).trackEvent("ApplicationSubmitted", event.message.telemetryProperties())

        // And a contact alert is created
        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.APPLICATION_SUBMITTED.code }
        assertThat(contact.alert, equalTo(true))
        assertThat(contact.eventId, equalTo(PersonGenerator.EVENT.id))
        assertThat(
            contact.externalReference,
            equalTo("$SERVICE_URN_BASE:application-submitted:68df9f6c-3fcb-4ec6-8fcf-96551cd9b080")
        )
    }

    @Test
    fun `application assessed creates an alert contact`() {
        // Given an application-assessed event
        val event = prepEvent("application-assessed", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        verify(telemetryService).trackEvent("ApplicationAssessed", event.message.telemetryProperties())

        // And a contact alert is created
        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.APPLICATION_ASSESSED.code }
        assertThat(contact.alert, equalTo(true))
        assertThat(contact.description, equalTo("Approved Premises Application Rejected"))
        assertThat(
            contact.notes,
            containsString(
                """
                |The application for a placement in an Approved Premises has been assessed for suitability and has been rejected.
                |Risk too low
                |Details of the application can be found here:
                """.trimMargin()
            )
        )
        assertThat(contact.eventId, equalTo(PersonGenerator.EVENT.id))
        assertThat(contact.date, equalTo(LocalDate.of(2022, 11, 30)))
        assertThat(
            contact.externalReference,
            equalTo("$SERVICE_URN_BASE:application-assessed:4e00113f-e46f-4a5f-93d3-4b60fed4cab0")
        )
    }

    @Test
    @Order(1)
    fun `booking made creates referral, nsi and contact`() {
        // Given a booking-made event
        val event = prepEvent("booking-made", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Send twice to verify we only create one referral
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        verify(telemetryService, times(2)).trackEvent("BookingMade", event.message.telemetryProperties())

        // And a contact alert is created
        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.BOOKING_MADE.code }
        assertThat(contact.alert, equalTo(true))
        assertThat(contact.description, equalTo("Approved Premises Booking for Hope House"))
        assertThat(
            contact.notes,
            equalTo(
                """
                Expected arrival: 30/01/2023
                Expected departure: 30/04/2023
                
                To view details of the Approved Premises booking, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/484b8b5e-6c3b-4400-b200-425bbe410713
                """.trimIndent()
            )
        )
        assertThat(contact.locationId, equalTo(OfficeLocationGenerator.DEFAULT.id))
        assertThat(contact.eventId, equalTo(PersonGenerator.EVENT.id))
        assertThat(contact.date, equalTo(LocalDate.of(2022, 11, 30)))
        assertThat(
            contact.externalReference,
            equalTo("$SERVICE_URN_BASE:booking-made:14c80733-4b6d-4f35-b724-66955aac320c")
        )

        val referrals = referralRepository.findAll()
            .filter { it.personId == contact.person.id && it.createdByUserId == UserGenerator.AUDIT_USER.id && it.eventId == contact.eventId }
        assertThat(referrals.size, equalTo(1))
        val referral = referrals.first()
        assertThat(
            referral.categoryId,
            equalTo(ReferenceDataGenerator.REFERRAL_CATEGORIES[ApprovedPremisesCategoryCode.VOLUNTARY_MAPPA.value]?.id)
        )
        assertThat(referral.externalReference, equalTo("${EXT_REF_BOOKING_PREFIX}14c80733-4b6d-4f35-b724-66955aac320c"))
        assertThat(referral.referralGroupId, equalTo(ReferenceDataGenerator.REFERRAL_GROUP.id))
        assertThat(referral.referralDate, equalTo(LocalDate.parse("2022-11-28")))
        assertThat(referral.activeArsonRiskId, equalTo(ReferenceDataGenerator.YN_UNKNOWN.id))
        assertThat(referral.disabilityIssuesId, equalTo(ReferenceDataGenerator.YN_UNKNOWN.id))
        assertThat(referral.singleRoomId, equalTo(ReferenceDataGenerator.YN_UNKNOWN.id))
        assertThat(referral.rohChildrenId, equalTo(ReferenceDataGenerator.RISK_UNKNOWN.id))
        assertThat(referral.rohOthersId, equalTo(ReferenceDataGenerator.RISK_UNKNOWN.id))
        assertThat(referral.rohKnownPersonId, equalTo(ReferenceDataGenerator.RISK_UNKNOWN.id))
        assertThat(referral.rohSelfId, equalTo(ReferenceDataGenerator.RISK_UNKNOWN.id))
        assertThat(referral.rohPublicId, equalTo(ReferenceDataGenerator.RISK_UNKNOWN.id))
        assertThat(referral.rohStaffId, equalTo(ReferenceDataGenerator.RISK_UNKNOWN.id))
        assertThat(referral.rohResidentsId, equalTo(ReferenceDataGenerator.RISK_UNKNOWN.id))
        assertTrue(referral.gangAffiliated)
        assertFalse(referral.sexOffender)

        val nsi = nsiRepository.findAll()
            .single { it.person.crn == contact.person.crn && it.type.code == NsiTypeCode.PRE_RELEASE_ARRIVAL.code }
        assertThat(nsi.externalReference, equalTo("${EXT_REF_PREARRIVAL_PREFIX}14c80733-4b6d-4f35-b724-66955aac320c"))
        assertThat(nsi.type.code, equalTo(NsiTypeCode.PRE_RELEASE_ARRIVAL.code))
        assertThat(nsi.status.code, equalTo(NsiStatusCode.AP_CASE_ALLOCATED.code))
        assertThat(nsi.referralDate, equalTo(LocalDate.parse("2022-11-30")))
        assertThat(nsi.expectedStartDate, equalTo(null))
        assertThat(nsi.event, equalTo(null))

        val caseAllocatedContact = contactRepository.findAll()
            .single { it.person.id == nsi.person.id && it.type.code == CASE_ALLOCATED.code && it.nsiId == nsi.id }
        assertThat(caseAllocatedContact.date, equalTo(nsi.statusDate.toLocalDate()))
    }

    @Test
    @Order(2)
    fun `person not arrived creates an alert contact`() {
        // Given a person-not-arrived event
        val event = prepEvent("person-not-arrived", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        verify(telemetryService).trackEvent("PersonNotArrived", event.message.telemetryProperties())

        // And a contact alert is created
        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.NOT_ARRIVED.code }
        assertThat(contact.alert, equalTo(true))
        assertThat(
            contact.notes,
            equalTo(
                """
            Notes about non-arrival.
            
            For more details, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/484b8b5e-6c3b-4400-b200-425bbe410713
                """.trimIndent()
            )
        )
        assertThat(contact.locationId, equalTo(OfficeLocationGenerator.DEFAULT.id))
        assertThat(contact.description, equalTo("Non Arrival Reason"))
        assertThat(contact.outcome?.code, equalTo(ContactOutcome.AP_NON_ARRIVAL_PREFIX + "D"))
        assertThat(contact.eventId, equalTo(PersonGenerator.EVENT.id))
        assertThat(contact.date, equalTo(LocalDate.of(2022, 11, 30)))
        assertThat(
            contact.externalReference,
            equalTo("$SERVICE_URN_BASE:person-not-arrived:14c80733-4b6d-4f35-b724-66955aac320c")
        )

        val referral =
            referralRepository.findAll().first { it.personId == contact.person.id && it.nonArrivalDate != null }
        assertThat(referral.nonArrivalDate, equalTo(contact.date))
        assertThat(referral.nonArrivalNotes, equalTo("Notes about non-arrival."))
        assertThat(referral.nonArrivalReasonId, equalTo(ReferenceDataGenerator.NON_ARRIVAL.id))
    }

    @Test
    @Order(3)
    fun `person arrived creates an alert contact and nsi`() {
        // Given a person-arrived event
        val event = prepEvent("person-arrived", wireMockServer.port())
        val arrival = ResourceLoader.file<EventDetails<PersonArrived>>("approved-premises-person-arrived")
        val details = arrival.eventDetails

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        verify(telemetryService).trackEvent("PersonArrived", event.message.telemetryProperties())

        // And a contact alert is created
        val alertContact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.ARRIVED.code }
        assertThat(alertContact.alert, equalTo(true))
        assertThat(
            alertContact.notes,
            equalTo(
                """
                Arrived a day late due to rail strike. Informed in advance by COM.

                For more details, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/484b8b5e-6c3b-4400-b200-425bbe410713
                """.trimIndent()
            )
        )
        assertThat(alertContact.locationId, equalTo(OfficeLocationGenerator.DEFAULT.id))
        assertThat(alertContact.eventId, equalTo(PersonGenerator.EVENT.id))
        assertThat(alertContact.tableName, equalTo("APPROVED_PREMISES_RESIDENCE"))
        assertThat(
            alertContact.externalReference,
            equalTo("$SERVICE_URN_BASE:person-arrived:14c80733-4b6d-4f35-b724-66955aac320c")
        )

        // And a residence NSI is created
        nsiRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == NsiTypeCode.APPROVED_PREMISES_RESIDENCE.code }
            .let { nsi ->
                assertThat(
                    nsi.notes,
                    equalTo(
                        """
                        Arrived a day late due to rail strike. Informed in advance by COM.
        
                        For more details, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/484b8b5e-6c3b-4400-b200-425bbe410713
                        """.trimIndent()
                    )
                )
                assertThat(nsi.event?.id, equalTo(PersonGenerator.EVENT.id))
                assertThat(nsi.externalReference, equalTo(EXT_REF_BOOKING_PREFIX + details.bookingId))
                assertThat(nsi.referralDate, equalTo(details.applicationSubmittedOn))
                assertNotNull(nsi.actualStartDate)
                assertThat(nsi.actualStartDate, isSameTimeAs(details.arrivedAt))

                val nsiReferralContact = contactRepository.findAll()
                    .single { it.nsiId == nsi.id && it.type.code == ContactTypeCode.NSI_REFERRAL.code }
                assertThat(nsiReferralContact.date, equalTo(details.arrivedAt.toLocalDate()))
                assertThat(nsiReferralContact.eventId, equalTo(PersonGenerator.EVENT.id))
                assertThat(nsiReferralContact.alert, equalTo(false))
            }

        // And a rehabilitative activity NSI is created
        nsiRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == NsiTypeCode.REHABILITATIVE_ACTIVITY.code }
            .let { nsi ->
                assertThat(
                    nsi.notes,
                    equalTo(
                        """
                        Arrived a day late due to rail strike. Informed in advance by COM.
        
                        For more details, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/484b8b5e-6c3b-4400-b200-425bbe410713
                        """.trimIndent()
                    )
                )
                assertThat(nsi.person.id, equalTo(PersonGenerator.DEFAULT.id))
                assertThat(nsi.event?.id, equalTo(PersonGenerator.EVENT.id))
                assertThat(nsi.externalReference, equalTo(EXT_REF_REHABILITATIVE_ACTIVITY_PREFIX + details.bookingId))
                assertThat(nsi.referralDate, equalTo(details.applicationSubmittedOn))
                assertThat(nsi.status.code, equalTo(NsiStatusCode.ACTIVE.code))
                assertNotNull(nsi.actualStartDate)
                assertThat(nsi.actualStartDate, isSameTimeAs(details.arrivedAt))

                val nsiReferralContact = contactRepository.findAll()
                    .single { it.nsiId == nsi.id && it.type.code == ContactTypeCode.NSI_REFERRAL.code }
                assertThat(nsiReferralContact.date, equalTo(details.arrivedAt.toLocalDate()))
                assertThat(nsiReferralContact.eventId, equalTo(PersonGenerator.EVENT.id))
                assertThat(nsiReferralContact.alert, equalTo(false))

                val nsiStatusChangeContact =
                    contactRepository.findAll().single { it.nsiId == nsi.id && it.type.code == "SMLI001" }
                assertThat(nsiStatusChangeContact.date, equalTo(details.arrivedAt.toLocalDate()))
                assertThat(nsiStatusChangeContact.eventId, equalTo(PersonGenerator.EVENT.id))
                assertThat(nsiStatusChangeContact.alert, equalTo(false))
            }

        // And the main address is updated to be that of the approved premises - consequently any existing main address is made previous
        val addresses = personAddressRepository.findAll().filter { it.personId == PersonGenerator.DEFAULT.id }
            .associateBy { it.id == PERSON_ADDRESS_ID }
        assertThat(addresses.size, equalTo(2))
        val previous = addresses[true]!!
        assertThat(previous.endDate, equalTo(details.arrivedAt.toLocalDate()))
        assertThat(previous.status.code, equalTo("P"))

        val main = addresses[false]!!
        val ap = AddressGenerator.Q001
        assertThat(main.status.code, equalTo("M"))
        assertNull(main.endDate)
        assertThat(main.startDate, equalTo(details.arrivedAt.toLocalDate()))
        assertThat(main.buildingName, equalTo(details.premises.name))
        assertThat(main.addressNumber, equalTo(ap.addressNumber))
        assertThat(main.streetName, equalTo(ap.streetName))
        assertThat(main.town, equalTo(ap.town))
        assertThat(main.postcode, equalTo(ap.postcode))
        assertThat(main.telephoneNumber, equalTo(ap.telephoneNumber))

        // And a domain event is published for the new address
        val domainEvent = channelManager.getChannel(topicName).receive()?.message as HmppsDomainEvent
        assertThat(domainEvent.eventType, equalTo("probation-case.address.created"))
        assertThat(domainEvent.crn(), equalTo(event.message.crn()))
        assertThat(domainEvent.additionalInformation["addressId"], equalTo(main.id))
        assertThat(domainEvent.additionalInformation["addressStatus"], equalTo("Main Address"))

        staffRepository.getByCode("N54A001")
        val residences = residenceRepository.findAll().filter { it.personId == alertContact.person.id }
        assertThat(residences.size, equalTo(1))
        val residence = residences.first()
        assertThat(residence.arrivalDate, isSameTimeAs(details.arrivedAt))
        assertThat(residence.expectedDepartureDate, equalTo(details.expectedDepartureOn))
        assertThat(alertContact.primaryKeyId, equalTo(residence.id))
    }

    @Test
    @Order(4)
    fun `person arrived with existing residence does not create duplicate`() {
        // Given a person-arrived event
        val event = prepEvent("person-arrived", wireMockServer.port())
        val arrival = ResourceLoader.file<EventDetails<PersonArrived>>("approved-premises-person-arrived")
        val details = arrival.eventDetails
        val externalReference = EXT_REF_BOOKING_PREFIX + details.bookingId

        // And a person with an existing un-managed residence
        val residenceBefore = residenceRepository.findAll().single { it.personId == PersonGenerator.DEFAULT.id }
        residenceBefore.arrivalDate = residenceBefore.arrivalDate.plusDays(1)
        residenceBefore.expectedDepartureDate = residenceBefore.expectedDepartureDate!!.plusDays(1)
        val nsi = nsiRepository.findByPersonIdAndExternalReference(PersonGenerator.DEFAULT.id, externalReference)!!
        nsiManagerRepository.findAll().filter { it.nsi.id == nsi.id }.also(nsiManagerRepository::deleteAll)
        nsiRepository.delete(nsi)

        // When the event is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        verify(telemetryService).trackEvent("PersonArrived", event.message.telemetryProperties())

        // And the residence details are updated in-place
        val residence = residenceRepository.findAll().single { it.personId == PersonGenerator.DEFAULT.id }
        assertThat(residence.arrivalDate, isSameTimeAs(details.arrivedAt))
        assertThat(residence.expectedDepartureDate, equalTo(details.expectedDepartureOn))
    }

    @Test
    @Order(4)
    fun `person departed creates a contact and closes nsi`() {
        val event = prepEvent("person-departed", wireMockServer.port())
        val departure = ResourceLoader.file<EventDetails<PersonDeparted>>("approved-premises-person-departed")
        val details = departure.eventDetails

        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent("PersonDeparted", event.message.telemetryProperties())

        val departureContact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.DEPARTED.code }
        assertThat(departureContact.alert, equalTo(false))
        assertThat(
            departureContact.notes,
            equalTo("For details, see the referral on the AP Service: ${details.applicationUrl}")
        )
        assertThat(departureContact.locationId, equalTo(OfficeLocationGenerator.DEFAULT.id))
        assertThat(departureContact.outcome?.code, equalTo("AP_N"))
        assertThat(departureContact.eventId, equalTo(PersonGenerator.EVENT.id))
        assertThat(departureContact.description, equalTo("Departed from Hope House"))
        assertThat(departureContact.date, equalTo(LocalDate.of(2023, 1, 16)))
        assertThat(
            departureContact.externalReference,
            equalTo("$SERVICE_URN_BASE:person-departed:14c80733-4b6d-4f35-b724-66955aac320c")
        )

        nsiRepository.findByPersonIdAndExternalReference(
            departureContact.person.id,
            EXT_REF_BOOKING_PREFIX + details.bookingId
        ).let { nsi ->
            assertNotNull(nsi)
            assertNotNull(nsi!!.actualEndDate)
            assertThat(nsi.actualEndDate, isSameTimeAs(details.departedAt))
            assertThat(nsi.active, equalTo(false))
            assertThat(nsi.outcome!!.code, equalTo("APRC"))

            val nsiTerminatedContact = contactRepository.findAll()
                .single { it.nsiId == nsi.id && it.type.code == ContactTypeCode.NSI_TERMINATED.code }
            assertThat(nsiTerminatedContact.alert, equalTo(false))
            assertThat(nsiTerminatedContact.notes, equalTo("NSI Terminated with Outcome: Description of APRC"))
            assertThat(nsiTerminatedContact.eventId, equalTo(PersonGenerator.EVENT.id))
            assertThat(nsiTerminatedContact.date, equalTo(LocalDate.of(2023, 1, 16)))
        }

        nsiRepository.findByPersonIdAndExternalReference(
            departureContact.person.id,
            EXT_REF_REHABILITATIVE_ACTIVITY_PREFIX + details.bookingId
        ).let { nsi ->
            assertNotNull(nsi)
            assertNotNull(nsi!!.actualEndDate)
            assertThat(nsi.actualEndDate, isSameTimeAs(details.departedAt))
            assertThat(nsi.active, equalTo(false))
            assertThat(nsi.outcome!!.code, equalTo("GRAS1"))
            assertThat(nsi.status.code, equalTo("COMP"))
        }

        val addresses = personAddressRepository.findAll().filter { it.personId == PersonGenerator.DEFAULT.id }
        assertThat(addresses.size, equalTo(2))
        addresses.forEach {
            assertNotNull(it.endDate)
            assertThat(it.status.code, equalTo("P"))
        }

        assertNull(personAddressRepository.findMainAddress(PersonGenerator.DEFAULT.id))

        val domainEvent = channelManager.getChannel(topicName).receive()?.message as HmppsDomainEvent
        assertThat(domainEvent.eventType, equalTo("probation-case.address.updated"))
        assertThat(domainEvent.crn(), equalTo(event.message.crn()))
        assertThat(domainEvent.additionalInformation["addressStatus"], equalTo("Previous Address"))

        val residence = residenceRepository.findAll().first { it.personId == departureContact.person.id }
        assertThat(residence.departureDate, isSameTimeAs(details.departedAt))
        assertThat(residence.departureReasonId, equalTo(ReferenceDataGenerator.ORDER_EXPIRED.id))
        assertThat(residence.moveOnCategoryId, equalTo(ReferenceDataGenerator.MC05.id))
    }

    @Test
    @Order(5)
    fun `booking changed updates referral`() {
        val event = prepEvent("booking-changed", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent("BookingChanged", event.message.telemetryProperties())

        val referral = referralRepository.findAll().first {
            it.personId == PersonGenerator.DEFAULT.id && it.eventId == PersonGenerator.EVENT.id
        }
        assertThat(referral.expectedArrivalDate, equalTo(LocalDate.parse("2023-08-14")))
        assertThat(referral.expectedDepartureDate, equalTo(LocalDate.parse("2023-08-30")))

        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.BOOKING_CHANGED.code }
        assertThat(
            contact.notes,
            equalTo(
                """
                |The expected arrival and/or departure dates for the booking have changed.
                |
                |Previous: 30/01/2023 to 30/04/2023
                |
                |Current: 14/08/2023 to 30/08/2023
                |
                |For more details, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/364145f9-0af8-488e-9901-b4c46cd9ba37
                """.trimMargin()
            )
        )
        assertThat(contact.locationId, equalTo(OfficeLocationGenerator.DEFAULT.id))
        assertThat(contact.eventId, equalTo(PersonGenerator.EVENT.id))
        assertThat(contact.date, equalTo(LocalDate.of(2023, 7, 25)))
        assertThat(
            contact.externalReference,
            equalTo("$SERVICE_URN_BASE:booking-changed:364145f9-0af8-488e-9901-b4c46cd9ba37:14c80733-4b6d-4f35-b724-66955aac320c")
        )
    }

    @Test
    @Order(6)
    fun `booking cancelled not allowed after arrival`() {
        val event = prepEvent("booking-cancelled", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent(
            "ApprovedPremisesFailureReport",
            event.message.telemetryProperties() + mapOf(
                "externalReference" to "urn:uk:gov:hmpps:approved-premises-service:booking:14c80733-4b6d-4f35-b724-66955aac320c",
                "arrivedAt" to "30/11/2022 14:51:30",
                "departedAt" to "16/01/2023 17:21:30",
                "reason" to "Cannot cancel booking as residency recorded"
            )
        )

        // contact should not be generated
        val contact = contactRepository.findAll()
            .singleOrNull { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.BOOKING_CANCELLED.code }
        assertNull(contact)

        // referral should not be deleted
        val referral = referralRepository.findAll().firstOrNull {
            it.personId == PersonGenerator.DEFAULT.id && it.eventId == PersonGenerator.EVENT.id
        }
        assertNotNull(referral)
    }

    @Test
    @Order(7)
    fun `application withdrawn creates a contact`() {
        val event = prepEvent("application-withdrawn", wireMockServer.port())


        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent("ApplicationWithdrawn", event.message.telemetryProperties())

        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.APPLICATION_WITHDRAWN.code }
        assertThat(contact.alert, equalTo(true))
        assertThat(
            contact.notes,
            equalTo(
                """
            Reason for application withdrawal
            
            For more details, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/484b8b5e-6c3b-4400-b200-425bbe410713
                """.trimIndent()
            )
        )
        assertThat(contact.eventId, equalTo(PersonGenerator.EVENT.id))
        assertThat(contact.date, equalTo(LocalDate.of(2023, 7, 24)))
        assertThat(
            contact.externalReference,
            equalTo("$SERVICE_URN_BASE:application-withdrawn:484b8b5e-6c3b-4400-b200-425bbe410713")
        )
    }

    @Test
    @Order(8)
    fun `booking cancelled creates a contact when no arrival`() {
        val event = prepEvent("booking-cancelled", wireMockServer.port())

        val ref = referralRepository.findAll().firstOrNull {
            it.personId == PersonGenerator.DEFAULT.id && it.eventId == PersonGenerator.EVENT.id
        }
        assertNotNull(ref!!)

        residenceRepository.findByReferralId(ref.id)?.also(residenceRepository::delete)

        preferredResidenceRepository.save(PreferredResidence(0, ref.id))
        assertTrue(preferredResidenceRepository.existsByApprovedPremisesReferralId(ref.id))

        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).trackEvent("BookingCancelled", event.message.telemetryProperties())

        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.BOOKING_CANCELLED.code }
        assertThat(contact.alert, equalTo(true))
        assertThat(
            contact.notes,
            equalTo(
                """
            Reason for application cancellation

            For more details, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/364145f9-0af8-488e-9901-b4c46cd9ba37
                """.trimIndent()
            )
        )
        assertThat(contact.locationId, equalTo(OfficeLocationGenerator.DEFAULT.id))
        assertThat(contact.description, equalTo("Booking cancelled for Hope House"))
        assertNull(contact.outcome)
        assertThat(contact.eventId, equalTo(PersonGenerator.EVENT.id))
        assertThat(contact.date, equalTo(LocalDate.of(2023, 7, 25)))
        assertThat(
            contact.externalReference,
            equalTo("$SERVICE_URN_BASE:booking-cancelled:14c80733-4b6d-4f35-b724-66955aac320c")
        )

        val referral = referralRepository.findAll().firstOrNull {
            it.personId == contact.person.id && it.eventId == contact.eventId
        }
        assertNull(referral)

        assertFalse(preferredResidenceRepository.existsByApprovedPremisesReferralId(ref.id))
    }
}
