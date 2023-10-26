package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.MethodOrderer
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
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.ApplicationSubmitted
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.BookingCancelled
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.BookingConfirmed
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.BookingProvisional
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.EventDetails
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.PersonDeparted
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

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

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `referral submitted message is processed correctly`() {
        val eventName = "referral-submitted"
        val event = prepEvent(eventName, wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)
        val eventDetails = ResourceLoader.file<EventDetails<ApplicationSubmitted>>("cas3-$eventName")
        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        MatcherAssert.assertThat(contact!!.type.code, Matchers.equalTo("EARS"))
    }

    @Test
    fun `booking cancelled message is processed correctly`() {
        val eventName = "booking-cancelled"
        val event = prepEvent(eventName, wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val eventDetails = ResourceLoader.file<EventDetails<BookingCancelled>>("cas3-$eventName")
        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        MatcherAssert.assertThat(contact!!.type.code, Matchers.equalTo("EACA"))
    }

    @Test
    fun `booking confirmed message is processed correctly`() {
        val eventName = "booking-confirmed"
        val event = prepEvent(eventName, wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val eventDetails = ResourceLoader.file<EventDetails<BookingConfirmed>>("cas3-$eventName")
        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        MatcherAssert.assertThat(contact!!.type.code, Matchers.equalTo("EACO"))
    }

    @Test
    fun `booking provisionally made message is processed correctly`() {
        val eventName = "booking-provisionally-made"
        val event = prepEvent(eventName, wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val eventDetails = ResourceLoader.file<EventDetails<BookingProvisional>>("cas3-$eventName")
        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        MatcherAssert.assertThat(contact!!.type.code, Matchers.equalTo("EABP"))
    }

    @Test
    fun `person arrived message is processed correctly`() {
        val eventName = "person-arrived"
        val event = prepEvent(eventName, wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val eventDetails = ResourceLoader.file<EventDetails<PersonArrived>>("cas3-$eventName")
        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        MatcherAssert.assertThat(contact!!.type.code, Matchers.equalTo("EAAR"))
    }

    @Test
    fun `person departed message is processed correctly`() {
        val eventName = "person-departed"
        val event = prepEvent(eventName, wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)

        val eventDetails = ResourceLoader.file<EventDetails<PersonDeparted>>("cas3-$eventName")
        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        MatcherAssert.assertThat(contact!!.type.code, Matchers.equalTo("EADP"))
    }

    @Test
    fun `person departed message update is processed correctly`() {
        val eventName = "person-departed-update"
        val event = prepEvent(eventName, wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then it is logged to telemetry
        Mockito.verify(telemetryService).notificationReceived(event)
        val oldEventDetails = ResourceLoader.file<EventDetails<PersonDeparted>>("cas3-person-departed")
        val eventDetails = ResourceLoader.file<EventDetails<PersonDeparted>>("cas3-$eventName")
        val contact = contactRepository.getByExternalReference(eventDetails.eventDetails.urn)

        MatcherAssert.assertThat(contact!!.type.code, Matchers.equalTo("EADP"))
        MatcherAssert.assertThat(contact.notes, Matchers.equalTo(oldEventDetails.eventDetails.noteText + "\n" + eventDetails.eventDetails.noteText))
    }
}
