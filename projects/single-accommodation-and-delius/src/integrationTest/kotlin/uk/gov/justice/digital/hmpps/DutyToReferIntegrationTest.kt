package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.messaging.Handler
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.model.DutyToRefer
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.service.ContactService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class DutyToReferIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}") private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val wireMockServer: WireMockServer,
    private val contactRepository: ContactRepository,
    private val contactService: ContactService,
    private val handler: Handler,
) {
    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `creates duty to refer created contact`() {
        val event = prepEvent("duty-to-refer-created", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        val contact = contactRepository.findAll().single { it.type.code == ContactType.DUTY_TO_REFER_CREATED }
        assertContact(contact, ContactType.DUTY_TO_REFER_CREATED, event.message.occurredAt)
        verify(telemetryService).trackEvent(
            "DutyToReferContactCreated",
            mapOf(
                "crn" to details.crn,
                "contactId" to contact.id.toString(),
                "contactType" to ContactType.DUTY_TO_REFER_CREATED,
                "externalReference" to "urn:uk:gov:hmpps:find-and-manage-accommodation:duty-to-refer:bb201dac-a8af-4ee0-a39f-0c62839e28f0",
            )
        )
    }

    @Test
    fun `creates duty to refer updated contact`() {
        val event = prepEvent("duty-to-refer-updated", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)

        val contact = contactRepository.findAll().single { it.type.code == ContactType.DUTY_TO_REFER_UPDATED }
        assertContact(contact, ContactType.DUTY_TO_REFER_UPDATED, event.message.occurredAt)
        verify(telemetryService).trackEvent(
            "DutyToReferContactCreated",
            mapOf(
                "crn" to details.crn,
                "contactId" to contact.id.toString(),
                "contactType" to ContactType.DUTY_TO_REFER_UPDATED,
                "externalReference" to "urn:uk:gov:hmpps:find-and-manage-accommodation:duty-to-refer:bb201dac-a8af-4ee0-a39f-0c62839e28f0",
            )
        )
    }

    @Test
    fun `unexpected event type is rejected`() {
        val event = prepEvent("duty-to-refer-created", wireMockServer.port()).run {
            copy(message = message.copy(eventType = "sas.unexpected"), attributes = MessageAttributes("sas.unexpected"))
        }

        assertThatThrownBy { handler.handle(event) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Unexpected event type sas.unexpected")
    }

    @Test
    fun `unexpected detail URL is rejected`() {
        val notification = prepEvent("duty-to-refer-created", wireMockServer.port()).run {
            copy(message = message.copy(detailUrl = "https://unexpected.example/details"))
        }

        assertThatThrownBy { handler.handle(notification) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Unexpected detail URL: https://unexpected.example")
    }

    @Test
    fun `missing CRN throws not found`() {
        assertThatThrownBy { contactService.createContact(details.copy(crn = "X999999"), ZonedDateTime.now(), "EADR") }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("Person with crn of X999999 not found")
    }

    @Test
    fun `missing contact types throws not found`() {
        assertThatThrownBy { contactService.createContact(details, ZonedDateTime.now(), "UNKNOWN") }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("ContactType with code of UNKNOWN not found")
    }

    private fun assertContact(contact: Contact, typeCode: String, occurredAt: ZonedDateTime) {
        val person = PersonGenerator.DEFAULT
        assertThat(contact.id).isPositive()
        assertThat(contact.person.id).isEqualTo(person.id)
        assertThat(contact.type.code).isEqualTo(typeCode)
        assertThat(contact.date).isEqualTo(occurredAt.toLocalDate())
        assertThat(contact.startTime.toInstant()).isEqualTo(occurredAt.toInstant())
        assertThat(contact.staffId).isEqualTo(person.manager.staff.id)
        assertThat(contact.teamId).isEqualTo(person.manager.team.id)
        assertThat(contact.providerId).isEqualTo(person.manager.probationAreaId)
        assertThat(contact.notes).isEqualTo("For more details, click here: ${details.viewUrl}")
        assertThat(contact.externalReference)
            .isEqualTo("urn:uk:gov:hmpps:find-and-manage-accommodation:duty-to-refer:${details.id}")
        assertThat(contact.createdByUserId).isEqualTo(UserGenerator.AUDIT_USER.id)
        assertThat(contact.lastUpdatedUserId).isEqualTo(UserGenerator.AUDIT_USER.id)
    }

    private val details = DutyToRefer(
        id = UUID.fromString("bb201dac-a8af-4ee0-a39f-0c62839e28f0"),
        crn = PersonGenerator.DEFAULT.crn,
        viewUrl = "https://single-accommodation-service-dev.hmpps.service.justice.gov.uk/cases/A000001/dtr/bb201dac-a8af-4ee0-a39f-0c62839e28f0/details",
    )
}
