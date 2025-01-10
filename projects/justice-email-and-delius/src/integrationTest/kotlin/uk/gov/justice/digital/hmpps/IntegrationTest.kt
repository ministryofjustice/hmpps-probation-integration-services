package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.atLeastOnce
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.Data
import uk.gov.justice.digital.hmpps.entity.Contact
import uk.gov.justice.digital.hmpps.entity.ContactRepository
import uk.gov.justice.digital.hmpps.entity.ContactType.Code.EMAIL
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.EmailMessage
import uk.gov.justice.digital.hmpps.messaging.Handler
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.get
import uk.gov.justice.digital.hmpps.service.MailboxService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class IntegrationTest {
    @Autowired
    lateinit var handler: Handler

    @Autowired
    lateinit var contactRepository: ContactRepository

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @MockBean
    lateinit var mailBoxService: MailboxService

    @Test
    fun `contact is created`() {
        val notification = Notification(get<EmailMessage>("successful-message"))
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)

        val contact = verifyContactCreated()
        assertThat(contact.type.code, equalTo(EMAIL.code))
        assertThat(contact.description, equalTo("Email - was involved in an incident"))
        assertThat(
            contact.notes, equalTo(
                """
            |This contact was created automatically from a forwarded email sent by example@justice.gov.uk at 12:34 on 01/01/2020.
            |Subject: A000001 was involved in an incident
            |
            |Example message
            |""".trimMargin()
            )
        )
        assertThat(
            contact.externalReference,
            equalTo("urn:uk:gov:hmpps:justice-email:00000000-0000-0000-0000-000000000000")
        )
        assertThat(contact.staffId, equalTo(Data.STAFF.id))
        assertThat(contact.teamId, equalTo(Data.MANAGER.teamId))
        assertThat(contact.providerId, equalTo(Data.MANAGER.providerId))
    }

    @Test
    fun `moj footer is removed if present`() {
        val notification = Notification(get<EmailMessage>("successful-message-with-footer"))
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)

        val contact = verifyContactCreated()
        assertThat(contact.type.code, equalTo(EMAIL.code))
        assertThat(contact.description, equalTo("Email - was involved in an incident"))
        assertThat(
            contact.notes, equalTo(
                """
            |This contact was created automatically from a forwarded email sent by example@justice.gov.uk at 10:24 on 10/01/2025.
            |Subject: A000001 was involved in an incident
            |
            |Example message
            |""".trimMargin()
            )
        )
        assertThat(
            contact.externalReference,
            equalTo("urn:uk:gov:hmpps:justice-email:00000000-0000-0000-0000-000000000000")
        )
        assertThat(contact.staffId, equalTo(Data.STAFF.id))
        assertThat(contact.teamId, equalTo(Data.MANAGER.teamId))
        assertThat(contact.providerId, equalTo(Data.MANAGER.providerId))
    }

    @Test
    fun `description is truncated if over 200 chars`() {
        val notification = Notification(get<EmailMessage>("successful-long-message"))
        handler.handle(notification)
        verify(telemetryService).notificationReceived(notification)

        val contact = verifyContactCreated()
        assertThat(contact.type.code, equalTo(EMAIL.code))
        assertThat(
            contact.description,
            equalTo("Email - was involved in an incident that has a very, very long subject that surpassed the 200 character limit and therefore has been truncated to meet the character limitations for the description i ~")
        )
        assertThat(
            contact.notes, equalTo(
                """
            |This contact was created automatically from a forwarded email sent by example@justice.gov.uk at 12:34 on 01/01/2020.
            |Subject: A000001 was involved in an incident that has a very, very long subject that surpassed the 200 character limit and therefore has been truncated to meet the character limitations for the description in Delius.
            |
            |Example message
            |""".trimMargin()
            )
        )
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

        val contact = verifyContactCreated()
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
        assertDoesNotThrow { handler.handle(notification) }
        verify(mailBoxService).onUnableToCreateContactFromEmail(any())
    }

    @Test
    fun `error when multiple staff records have the same email address`() {
        val notification = Notification(get<EmailMessage>("multiple-staff"))
        val exception = assertThrows<IllegalStateException> { handler.handle(notification) }
        assertThat(exception.message, equalTo("Multiple staff records found for duplicate@justice.gov.uk"))
    }

    @Test
    fun `error for unexpected source email address`() {
        val notification = Notification(get<EmailMessage>("non-justice-email"))
        val exception = assertThrows<IllegalArgumentException> { handler.handle(notification) }
        assertThat(
            exception.message,
            equalTo("Email address does not end with @justice.gov.uk or @digital.justice.gov.uk")
        )
    }

    @Test
    fun `converts html to text`() {
        val notification = Notification(
            get<EmailMessage>("successful-message").copy(
                bodyContent = """
                    <p>Paragraph 1
                    <p>Paragraph 2 with <strong>bold</strong> text</p>
                    <ul>
                        <li>List item 1</li>
                        <li>List item 2
                    </ul>
                    Text<br/>with<br>new lines
                """.trimIndent()
            )
        )
        handler.handle(notification)
        val contact = verifyContactCreated()
        assertThat(
            contact.notes, equalTo(
                """
                This contact was created automatically from a forwarded email sent by example@justice.gov.uk at 12:34 on 01/01/2020.
                Subject: A000001 was involved in an incident
                
                Paragraph 1
                
                Paragraph 2 with **bold** text
                
                * List item 1
                * List item 2
                
                Text  
                with  
                new lines
                
            """.trimIndent()
            )
        )
    }

    private fun verifyContactCreated(): Contact {
        val contactId = with(argumentCaptor<Map<String, String>>()) {
            verify(telemetryService).trackEvent(eq("CreatedContact"), capture(), any())
            firstValue["contactId"]!!.toLong()
        }
        return contactRepository.findById(contactId).orElseThrow()
    }
}
