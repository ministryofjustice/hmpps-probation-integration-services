package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.core.IsInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.integrations.randm.Attendance
import uk.gov.justice.digital.hmpps.integrations.randm.Behaviour
import uk.gov.justice.digital.hmpps.integrations.randm.ReferAndMonitorClient
import uk.gov.justice.digital.hmpps.integrations.randm.ReferralSession
import uk.gov.justice.digital.hmpps.integrations.randm.SessionFeedback
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.SessionAppointmentSubmitted
import uk.gov.justice.digital.hmpps.service.AppointmentService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class FeedbackSubmittedTest {

    @Mock
    lateinit var ramClient: ReferAndMonitorClient

    @Mock
    lateinit var appointmentService: AppointmentService

    @InjectMocks
    lateinit var feedbackSubmitted: FeedbackSubmitted

    @Test
    fun `handle method converts exceptions to failures`() {
        whenever(ramClient.getSession(any())).thenReturn(
            ReferralSession(
                "a-123-b",
                1,
                ZonedDateTime.now(),
                456,
                SessionFeedback(Attendance("yes", ZonedDateTime.now()), Behaviour(true))
            )
        )
        whenever(appointmentService.updateOutcome(any())).thenThrow(IllegalStateException("Something went wrong"))

        val res = feedbackSubmitted.sessionAppointmentSubmitted(
            HmppsDomainEvent(
                SessionAppointmentSubmitted.name,
                1,
                "https://somehost.gov.uk",
                ZonedDateTime.now(),
                additionalInformation = AdditionalInformation(
                    mutableMapOf(
                        "referralId" to "a-123-b",
                        "contractType" to mapOf("code" to "ACC", "name" to "Accommodation"),
                        "providerName" to "Provider XYZ",
                        "url" to "http://baseUrl/path/to/ui"
                    )
                ),
                personReference = PersonReference(listOf(PersonIdentifier("CRN", "X123456")))
            )
        )

        assertThat(res, IsInstanceOf(EventProcessingResult.Failure::class.java))
        assertThat(
            (res as EventProcessingResult.Failure).exception.message,
            equalTo("Something went wrong")
        )
    }

    @Test
    fun `session not found raises failure`() {
        whenever(ramClient.getSession(any())).thenReturn(null)

        val res = feedbackSubmitted.sessionAppointmentSubmitted(
            HmppsDomainEvent(
                SessionAppointmentSubmitted.name,
                1,
                "DetailUrl",
                ZonedDateTime.now()
            )
        )

        assertThat(res, IsInstanceOf(EventProcessingResult.Failure::class.java))
        assertThat(
            (res as EventProcessingResult.Failure).exception.message,
            equalTo("Unable to retrieve session: DetailUrl")
        )
    }
}
