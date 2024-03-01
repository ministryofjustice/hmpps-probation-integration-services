package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.ContactRepository
import uk.gov.justice.digital.hmpps.entity.ContactType.Companion.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.entity.ContactType.Companion.REFERRAL_UPDATED
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.time.LocalDate
import java.time.ZonedDateTime

@SpringBootTest
@ExtendWith(OutputCaptureExtension::class)
internal class IntegrationTest {
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

    @Test
    fun `application submitted`() {
        // Given a message
        val event = prepEvent("application-submitted", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then a contact is created
        val contact = contactRepository.findAll().single { it.type.code == REFERRAL_SUBMITTED }
        assertThat(contact.date, equalTo(LocalDate.of(2020, 1, 1)))
        assertThat(
            contact.startTime.toInstant(),
            equalTo(ZonedDateTime.of(2020, 1, 1, 12, 34, 56, 0, EuropeLondon).toInstant())
        )
        assertThat(contact.staffId, equalTo(1))
        assertThat(contact.teamId, equalTo(2))
        assertThat(contact.probationAreaId, equalTo(3))
        assertThat(contact.sensitive, equalTo(false))
        assertThat(
            contact.notes,
            equalTo("Details of the application can be found here: https://example.com/application/00000000-0000-0000-0000-000000000001")
        )
        assertThat(contact.description, nullValue())
        assertThat(
            contact.externalReference,
            equalTo("urn:hmpps:cas2:application-submitted:00000000-0000-0000-0000-000000000001")
        )

        // And it is logged to telemetry
        verify(telemetryService).notificationReceived(event)
        verify(telemetryService).trackEvent(
            "ApplicationSubmitted",
            mapOf(
                "crn" to "A000001",
                "detailUrl" to "http://localhost:${wireMockServer.port()}/approved-premises-api/events/cas2/application-submitted/1"
            ),
            mapOf()
        )
    }

    @Test
    fun `application status updated`() {
        // Given a message
        val event = prepEvent("application-status-updated", wireMockServer.port())

        // When it is received
        channelManager.getChannel(queueName).publishAndWait(event)

        // Then a contact is created
        val contact = contactRepository.findAll().single { it.type.code == REFERRAL_UPDATED }
        assertThat(contact.externalReference, equalTo("urn:hmpps:cas2:application-status-updated:1"))
        assertThat(contact.description, equalTo("CAS2 Referral Updated - More information requested"))
        assertThat(
            contact.notes, equalTo(
                """
                Application status was updated to: More information requested - More information about the application has been requested from the POM (Prison Offender Manager).
                
                Details: More information about the application has been requested from the POM (Prison Offender Manager).
                * Personal Information
                * Health Needs

                Details of the application can be found here: https://example.com/application/00000000-0000-0000-0000-000000000001
                """.trimIndent()
            )
        )

        // And it is logged to telemetry
        verify(telemetryService).trackEvent(
            "ApplicationStatusUpdated",
            mapOf(
                "crn" to "A000001",
                "detailUrl" to "http://localhost:${wireMockServer.port()}/approved-premises-api/events/cas2/application-status-updated/1",
                "applicationId" to "00000000-0000-0000-0000-000000000001",
                "status" to "moreInfoRequested"
            ),
            mapOf()
        )

        // And duplicate messages are ignored
        channelManager.getChannel(queueName).publishAndWait(event)
        assertThat(contactRepository.findAll().filter { it.type.code == REFERRAL_UPDATED }, hasSize(1))
        verify(telemetryService).trackEvent(
            "ContactAlreadyExists",
            mapOf("urn" to "urn:hmpps:cas2:application-status-updated:1"),
            mapOf()
        )
    }

    @Test
    fun `application submitted not found enabled`(output: CapturedOutput) {
        // Given a message
        val event = prepEvent("application-submitted-not-found", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)
        //Assert that only 1 trackEvent for Notification Received has occurred
        verify(telemetryService, Mockito.times(1)).trackEvent(any(), any(), any())
        //Assert that expected exception exists in output
        assertThat(output.all, containsString("No DomainEvent with an ID of 3333 could be found"))
    }

    @Test
    fun `application status not found enabled`(output: CapturedOutput) {
        // Given a message
        val event = prepEvent("application-status-updated-not-found", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(event)
        //Assert that only 1 trackEvent for Notification Received has occurred
        verify(telemetryService, Mockito.times(1)).trackEvent(any(), any(), any())
        //Assert that expected exception exists in output
        assertThat(output.all, containsString("No DomainEvent with an ID of 4444 could be found"))
    }
}
