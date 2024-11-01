package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.Data
import uk.gov.justice.digital.hmpps.entity.ContactRepository
import uk.gov.justice.digital.hmpps.entity.ContactType.Code.EMAIL_TEXT_FROM_OTHER
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.EmailMessage
import uk.gov.justice.digital.hmpps.messaging.Handler
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.get
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class IntegrationTest {
    @Autowired
    lateinit var handler: Handler

    @Autowired
    lateinit var contactRepository: ContactRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `contact is created`() {
        val notification = Notification(get<EmailMessage>("successful-message"))
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)

        val contactId = with(argumentCaptor<Map<String, String>>()) {
            verify(telemetryService).trackEvent(eq("CreatedContact"), capture(), any())
            firstValue["contactId"]!!.toLong()
        }
        val contact = contactRepository.findById(contactId).orElseThrow()
        assertThat(contact.type.code, equalTo(EMAIL_TEXT_FROM_OTHER.code))
        assertThat(contact.notes, equalTo("Example message"))
        assertThat(
            contact.externalReference,
            equalTo("urn:uk:gov:hmpps:justice-email:00000000-0000-0000-0000-000000000000")
        )
        assertThat(contact.staffId, equalTo(Data.STAFF.id))
        assertThat(contact.teamId, equalTo(Data.MANAGER.teamId))
        assertThat(contact.providerId, equalTo(Data.MANAGER.providerId))
    }

    @Test
    fun `allocates contact to manager if sender has no staff record`() {
        val notification = Notification(get<EmailMessage>("no-staff"))
        handler.handle(notification)
        verify(telemetryService, atLeastOnce()).notificationReceived(notification)

        val contactId = with(argumentCaptor<Map<String, String>>()) {
            verify(telemetryService).trackEvent(eq("CreatedContact"), capture(), any())
            firstValue["contactId"]!!.toLong()
        }
        val contact = contactRepository.findById(contactId).orElseThrow()
        assertThat(contact.staffId, equalTo(Data.MANAGER.staffId))
    }

    @Test
    fun `error when multiple crns`() {
        val notification = Notification(get<EmailMessage>("multiple-crns"))
        val exception = assertThrows<IllegalArgumentException> { handler.handle(notification) }
        assertThat(exception.message, equalTo("Multiple CRNs in message subject"))
    }

    @Test
    fun `error when missing crn`() {
        val notification = Notification(get<EmailMessage>("no-crn"))
        val exception = assertThrows<IllegalArgumentException> { handler.handle(notification) }
        assertThat(exception.message, equalTo("No CRN in message subject"))
    }
}
