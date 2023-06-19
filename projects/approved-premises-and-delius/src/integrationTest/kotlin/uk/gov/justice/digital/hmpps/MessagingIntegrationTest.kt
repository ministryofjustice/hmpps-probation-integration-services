package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.EventDetails
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonDeparted
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ReferralRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ResidenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.Nsi.Companion.EXT_REF_BOOKING_PREFIX
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddressRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(OrderAnnotation::class)
internal class MessagingIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

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

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    private lateinit var referralRepository: ReferralRepository

    @Autowired
    private lateinit var residenceRepository: ResidenceRepository

    @Test
    fun `application submission creates an alert contact`() {
        // Given an application-submitted event
        val event = prepEvent("application-submitted", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(event)
        verify(telemetryService).trackEvent("ApplicationSubmitted", event.message.telemetryProperties())

        // And a contact alert is created
        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.APPLICATION_SUBMITTED.code }
        assertThat(contact.alert, equalTo(true))
    }

    @Test
    fun `application assessed creates an alert contact`() {
        // Given an application-assessed event
        val event = prepEvent("application-assessed", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(event)
        verify(telemetryService).trackEvent("ApplicationAssessed", event.message.telemetryProperties())

        // And a contact alert is created
        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.APPLICATION_ASSESSED.code }
        assertThat(contact.alert, equalTo(true))
        assertThat(contact.description, equalTo("Approved Premises Application Rejected"))
        assertThat(contact.notes, equalTo("Risk too low"))
    }

    @Test
    @Order(1)
    fun `booking made creates referral and contact`() {
        // Given a booking-made event
        val event = prepEvent("booking-made", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Send twice to verify we only create one referral
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        verify(telemetryService, times(2)).notificationReceived(event)
        verify(telemetryService, times(2)).trackEvent("BookingMade", event.message.telemetryProperties())

        // And a contact alert is created
        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.BOOKING_MADE.code }
        assertThat(contact.alert, equalTo(true))
        assertThat(contact.description, equalTo("Approved Premises Booking for Hope House"))
        assertThat(
            contact.notes,
            equalTo("To view details of the Approved Premises booking, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/484b8b5e-6c3b-4400-b200-425bbe410713")
        )
        assertThat(contact.locationId, equalTo(OfficeLocationGenerator.DEFAULT.id))

        val referrals = referralRepository.findAll()
            .filter { it.personId == contact.person.id && it.createdByUserId == UserGenerator.AUDIT_USER.id && it.referralDate == contact.date }
        assertThat(referrals.size, equalTo(1))
        val referral = referrals.first()
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
    }

    @Test
    @Order(2)
    fun `person not arrived creates an alert contact`() {
        // Given a person-not-arrived event
        val event = prepEvent("person-not-arrived", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(event)
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

        val referral = referralRepository.findAll().first { it.personId == contact.person.id }
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
        verify(telemetryService).notificationReceived(event)
        verify(telemetryService).trackEvent("PersonArrived", event.message.telemetryProperties())

        // And a contact alert is created
        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.ARRIVED.code }
        assertThat(contact.alert, equalTo(true))
        assertThat(
            contact.notes,
            equalTo(
                """
                Arrived a day late due to rail strike. Informed in advance by COM.

                For more details, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/484b8b5e-6c3b-4400-b200-425bbe410713
                """.trimIndent()
            )
        )
        assertThat(contact.locationId, equalTo(OfficeLocationGenerator.DEFAULT.id))

        // And a residence NSI is created
        val nsi = nsiRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == NsiTypeCode.APPROVED_PREMISES_RESIDENCE.code }
        assertThat(
            nsi.notes,
            equalTo(
                """
                Arrived a day late due to rail strike. Informed in advance by COM.

                For more details, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/484b8b5e-6c3b-4400-b200-425bbe410713
                """.trimIndent()
            )
        )
        assertThat(nsi.externalReference, equalTo(EXT_REF_BOOKING_PREFIX + details.bookingId))
        assertThat(nsi.referralDate, equalTo(details.applicationSubmittedOn))
        assertNotNull(nsi.actualStartDate)
        assertThat(
            nsi.actualStartDate!!.withZoneSameInstant(EuropeLondon),
            equalTo(details.arrivedAt.withZoneSameInstant(EuropeLondon))
        )

        // And the main address is updated to be that of the approved premises - consequently any existing main address is made previous
        val addresses = personAddressRepository.findAll().filter { it.personId == PersonGenerator.DEFAULT.id }
            .associateBy { it.id == AddressGenerator.PERSON_ADDRESS.id }
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

        val residences = residenceRepository.findAll().filter { it.personId == contact.person.id }
        assertThat(residences.size, equalTo(1))
        val residence = residences.first()
        assertThat(residence.arrivalDate, equalTo(contact.date))
    }

    @Test
    @Order(4)
    fun `person departed creates a contact and closes nsi`() {
        val event = prepEvent("person-departed", wireMockServer.port())
        val departure = ResourceLoader.file<EventDetails<PersonDeparted>>("approved-premises-person-departed")
        val details = departure.eventDetails

        channelManager.getChannel(queueName).publishAndWait(event)

        verify(telemetryService).notificationReceived(event)
        verify(telemetryService).trackEvent("PersonDeparted", event.message.telemetryProperties())

        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.DEPARTED.code }
        assertThat(contact.alert, equalTo(false))
        assertThat(
            contact.notes,
            equalTo("For details, see the referral on the AP Service: ${details.applicationUrl}")
        )
        assertThat(contact.locationId, equalTo(OfficeLocationGenerator.DEFAULT.id))
        assertThat(contact.outcome?.code, equalTo("AP_N"))

        val nsi = nsiRepository.findByExternalReference(EXT_REF_BOOKING_PREFIX + details.bookingId)
        assertNotNull(nsi)
        assertNotNull(nsi!!.actualEndDate)
        assertThat(nsi.actualEndDate!!, isCloseTo(details.departedAt))
        assertThat(nsi.active, equalTo(false))

        val addresses = personAddressRepository.findAll().filter { it.personId == PersonGenerator.DEFAULT.id }
        assertThat(addresses.size, equalTo(2))
        addresses.forEach {
            assertNotNull(it.endDate)
            assertThat(it.status.code, equalTo("P"))
        }

        assertNull(personAddressRepository.findMainAddress(PersonGenerator.DEFAULT.id))

        val residence = residenceRepository.findAll().first { it.personId == contact.person.id }
        assertThat(residence.departureDate, equalTo(contact.date))
        assertThat(residence.departureReasonId, equalTo(ReferenceDataGenerator.ORDER_EXPIRED.id))
        assertThat(residence.moveOnCategoryId, equalTo(ReferenceDataGenerator.MC05.id))
    }
}
