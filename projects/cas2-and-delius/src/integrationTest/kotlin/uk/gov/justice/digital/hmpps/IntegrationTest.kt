package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.ContactRepository
import uk.gov.justice.digital.hmpps.entity.ContactType.Companion.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.time.LocalDate
import java.time.ZonedDateTime

@SpringBootTest
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
}
