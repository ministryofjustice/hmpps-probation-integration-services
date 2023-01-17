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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.EventDetails
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonDeparted
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.Nsi.Companion.EXT_REF_BOOKING_PREFIX
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddressRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(OrderAnnotation::class)
internal class IntegrationTest {
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
    lateinit var nsiRepository: NsiRepository

    @Autowired
    lateinit var personAddressRepository: PersonAddressRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    @Order(1)
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
    @Order(2)
    fun `empty approved premises returns 200 with empty results`() {
        val approvedPremises = ApprovedPremisesGenerator.NO_STAFF
        mockMvc
            .perform(get("/approved-premises/${approvedPremises.code.code}/staff").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.numberOfElements", equalTo(0)))
            .andExpect(jsonPath("$.totalElements", equalTo(0)))
    }

    @Test
    @Order(3)
    fun `non-existent approved premises returns 404`() {
        mockMvc
            .perform(get("/approved-premises/NOTFOUND/staff").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", equalTo("Approved Premises with code of NOTFOUND not found")))
    }

    @Test
    @Order(4)
    fun `approved premises key workers only are returned successfully`() {
        val approvedPremises = ApprovedPremisesGenerator.DEFAULT
        mockMvc
            .perform(
                get("/approved-premises/${approvedPremises.code.code}/staff?keyWorker=true").withOAuth2Token(
                    wireMockServer
                )
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.numberOfElements", equalTo(1)))
            .andExpect(jsonPath("$.content[*].name.surname", equalTo(listOf("Key-worker"))))
            .andExpect(jsonPath("$.content[*].keyWorker", equalTo(listOf(true))))
    }

    @Test
    @Order(5)
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
    @Order(6)
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
    @Order(7)
    fun `booking made creates an alert contact`() {
        // Given a booking-made event
        val event = prepEvent("booking-made", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(event)
        verify(telemetryService).trackEvent("BookingMade", event.message.telemetryProperties())

        // And a contact alert is created
        val contact = contactRepository.findAll()
            .single { it.person.crn == event.message.crn() && it.type.code == ContactTypeCode.BOOKING_MADE.code }
        assertThat(contact.alert, equalTo(true))
        assertThat(contact.description, equalTo("Approved Premises Booking for Hope House"))
        assertThat(
            contact.notes,
            equalTo("To view details of the Approved Premises booking, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/484b8b5e-6c3b-4400-b200-425bbe410713")
        )
    }

    @Test
    @Order(8)
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
            We learnt that Mr Smith is in hospital.
            
            For more details, click here: https://approved-premises-dev.hmpps.service.justice.gov.uk/applications/484b8b5e-6c3b-4400-b200-425bbe410713
                """.trimIndent()
            )
        )
    }

    @Test
    @Order(9)
    fun `person arrived creates an alert contact and nsi`() {
        // Given a person-not-arrived event
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
        assertThat(
            nsi.externalReference,
            equalTo("urn:uk:gov:hmpps:approved-premises-service:booking:${details.bookingId}")
        )
        assertThat(
            nsi.referralDate.withZoneSameInstant(EuropeLondon),
            equalTo(details.applicationSubmittedOn.withZoneSameInstant(EuropeLondon))
        )
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
    }

    @Test
    @Order(10)
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
    }
}
