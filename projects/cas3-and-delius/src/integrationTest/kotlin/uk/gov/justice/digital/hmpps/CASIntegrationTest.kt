package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.*
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class CASIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var addressRepository: PersonAddressRepository

    @Autowired
    lateinit var personRepository: PersonRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    @Order(1)
    fun `referral submitted message is processed correctly`() {
        val eventName = "referral-submitted"
        val event = prepEvent(eventName, wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)
        val eventDetails = ResourceLoader.file<EventDetails<ApplicationSubmitted>>("cas3-$eventName")
        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        assertThat(contact!!.type.code, equalTo("EARS"))
    }

    @Test
    @Order(2)
    fun `booking cancelled message is processed correctly`() {
        val eventName = "booking-cancelled"
        val event = prepEvent(eventName, wireMockServer.port())
        val eventDetails = ResourceLoader.file<EventDetails<BookingCancelled>>("cas3-$eventName")
        val eventDetailsCopy = eventDetails.copy(timestamp = ZonedDateTime.now().minusSeconds(3))
        wireMockServer.stubFor(
            get(urlEqualTo("/cas3-api/events/booking-cancelled/1234"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(eventDetailsCopy))
                )
        )

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        assertThat(contact!!.type.code, equalTo("EACA"))
        assertThat(contact.teamId, equalTo(ProviderGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(ProviderGenerator.DEFAULT_STAFF.id))
    }

    @Test
    @Order(3)
    fun `booking confirmed message is processed correctly`() {
        val eventName = "booking-confirmed"
        val event = prepEvent(eventName, wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val eventDetails = ResourceLoader.file<EventDetails<BookingConfirmed>>("cas3-$eventName")
        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        assertThat(contact!!.type.code, equalTo("EACO"))
        assertThat(contact.teamId, equalTo(ProviderGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(ProviderGenerator.DEFAULT_STAFF.id))
    }

    @Test
    @Order(4)
    fun `booking provisionally made message is processed correctly`() {
        val eventName = "booking-provisionally-made"
        val event = prepEvent(eventName, wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val eventDetails = ResourceLoader.file<EventDetails<BookingProvisional>>("cas3-$eventName")
        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        assertThat(contact!!.type.code, equalTo("EABP"))
        assertThat(contact.teamId, equalTo(ProviderGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(ProviderGenerator.DEFAULT_STAFF.id))
    }

    @Test
    @Order(5)
    fun `person arrived message is processed correctly`() {
        val eventName = "person-arrived"
        val event = prepEvent(eventName, wireMockServer.port())
        val eventDetails = ResourceLoader.file<EventDetails<PersonArrived>>("cas3-$eventName")
        val eventDetailsCopy = eventDetails.copy(timestamp = ZonedDateTime.now().minusSeconds(3))
        wireMockServer.stubFor(
            get(urlEqualTo("/cas3-api/events/person-arrived/1234"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(eventDetailsCopy))
                )
        )

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        assertThat(contact!!.type.code, equalTo("EAAR"))
        assertThat(contact.teamId, equalTo(ProviderGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(ProviderGenerator.DEFAULT_STAFF.id))

        val person = personRepository.findByCrn(event.message.crn())
        val address = addressRepository.findMainAddress(person!!.id)

        assertThat(address!!.type.code, equalTo("A17"))
        assertThat(address.town, equalTo(eventDetails.eventDetails.premises.town))
        assertThat(address.streetName, equalTo(eventDetails.eventDetails.premises.addressLine1))
        assertThat(address.county, equalTo(eventDetails.eventDetails.premises.region))
        assertThat(address.postcode, equalTo(eventDetails.eventDetails.premises.postcode))
    }

    @Test
    @Order(7)
    fun `person departed message is processed correctly`() {
        val eventName = "person-departed"
        val event = prepEvent(eventName, wireMockServer.port())
        val eventDetails = ResourceLoader.file<EventDetails<PersonDeparted>>("cas3-$eventName")
        val eventDetailsCopy = eventDetails.copy(timestamp = ZonedDateTime.now().minusSeconds(3))
        wireMockServer.stubFor(
            get(urlEqualTo("/cas3-api/events/person-departed/1234"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(eventDetailsCopy))
                )
        )

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        assertThat(contact!!.type.code, equalTo("EADP"))
        val person = personRepository.findByCrn(event.message.crn())
        val address = addressRepository.findAll().filter { it.personId == person?.id }[0]
        assertThat(address!!.status.code, equalTo("P"))
        assertThat(contact.teamId, equalTo(ProviderGenerator.DEFAULT_TEAM.id))
        assertThat(contact.staffId, equalTo(ProviderGenerator.DEFAULT_STAFF.id))
    }

    @Test
    @Order(8)
    fun `person departed message update is processed correctly`() {
        val eventName = "person-departed-update"
        val event = prepEvent(eventName, wireMockServer.port())
        val existingEventDetails = ResourceLoader.file<EventDetails<PersonDeparted>>("cas3-person-departed")
        val existingContact = contactRepository.getByExternalReference(existingEventDetails.eventDetails.urn)
        val existingNotes = existingContact!!.notes
        val eventDetails = ResourceLoader.file<EventDetails<PersonDeparted>>("cas3-$eventName")
        val eventDetailsCopy = eventDetails.copy(timestamp = ZonedDateTime.now())
        wireMockServer.stubFor(
            get(urlEqualTo("/cas3-api/events/person-departed/12345"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(eventDetailsCopy))
                )
        )

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)
        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        assertThat(
            contact!!.notes,
            equalTo(eventDetails.eventDetails.noteText + System.lineSeparator() + existingNotes)
        )
    }

    @Test
    @Order(6)
    fun `person arrived updated message is processed correctly`() {
        val eventName = "person-arrived-update"
        val event = prepEvent(eventName, wireMockServer.port())
        val existingEventDetails = ResourceLoader.file<EventDetails<PersonArrived>>("cas3-person-arrived-update")
        val existingContact = contactRepository.getByExternalReference(existingEventDetails.eventDetails.urn)
        val existingNotes = existingContact!!.notes
        val eventDetails = ResourceLoader.file<EventDetails<PersonArrived>>("cas3-$eventName")
        val eventDetailsCopy = eventDetails.copy(timestamp = ZonedDateTime.now())
        wireMockServer.stubFor(
            get(urlEqualTo("/cas3-api/events/person-arrived/12345"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(eventDetailsCopy))
                )
        )

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        assertThat(
            contact!!.notes,
            equalTo(
                listOf(
                    existingNotes,
                    "Address details were updated: ${DeliusDateTimeFormatter.format(eventDetailsCopy.timestamp)}",
                    eventDetails.eventDetails.noteText
                ).joinToString(System.lineSeparator())
            )
        )

        val mainAddress = addressRepository.findMainAddress(contact.offenderId)
        assertThat(mainAddress?.startDate, equalTo(eventDetails.eventDetails.arrivedAt.toLocalDate()))
    }

    @Test
    @Order(9)
    fun `booking cancelled updated message is processed correctly`() {
        val eventName = "booking-cancelled-update"
        val event = prepEvent(eventName, wireMockServer.port())
        val existingEventDetails = ResourceLoader.file<EventDetails<BookingCancelled>>("cas3-booking-cancelled-update")
        val existingContact = contactRepository.getByExternalReference(existingEventDetails.eventDetails.urn)
        val existingNotes = existingContact!!.notes
        val eventDetails = ResourceLoader.file<EventDetails<BookingCancelled>>("cas3-$eventName")
        val eventDetailsCopy = eventDetails.copy(timestamp = ZonedDateTime.now())
        wireMockServer.stubFor(
            get(urlEqualTo("/cas3-api/events/booking-cancelled/12345"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(eventDetailsCopy))
                )
        )

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        assertThat(
            contact!!.notes,
            equalTo(eventDetails.eventDetails.noteText + System.lineSeparator() + existingNotes)
        )
    }
}
