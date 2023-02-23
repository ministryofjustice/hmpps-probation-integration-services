package uk.gov.justice.digital.hmpps.messaging

import uk.gov.justice.digital.hmpps.integrations.randm.ReferAndMonitorClient
import uk.gov.justice.digital.hmpps.integrations.randm.ReferralSession
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.InitialAppointmentSubmitted
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.SessionAppointmentSubmitted
import uk.gov.justice.digital.hmpps.service.AppointmentService
import java.net.URI

// @Component deactivated until completed
class FeedbackSubmitted(
    private val ramClient: ReferAndMonitorClient,
    private val appointmentService: AppointmentService
) : DomainEventHandler {
    override val handledEvents = mapOf(
        InitialAppointmentSubmitted to ::initialAppointmentSubmitted,
        SessionAppointmentSubmitted to ::sessionAppointmentSubmitted
    )

    fun initialAppointmentSubmitted(event: HmppsDomainEvent): EventProcessingResult {
        TODO()
    }

    fun sessionAppointmentSubmitted(event: HmppsDomainEvent): EventProcessingResult = handle {
        val appointment = ramClient.getSession(URI(event.detailUrl!!))
        TODO()
    }
}

private fun ReferralSession.appointmentOutcome(
    crn: String,
    referralId: String,
    contractType: String,
    providerName: String,
    url: String
) = UpdateAppointmentOutcome(
    appointmentId,
    crn,
    AppointmentReferral(referralId, contractType),
    Attended.of(sessionFeedback.attendance.attended),
    sessionFeedback.behaviour.notifyProbationPractitioner,
    Provider(providerName),
    url
)

data class UpdateAppointmentOutcome(
    val id: Long,
    val crn: String,
    val referral: AppointmentReferral,
    val attended: Attended,
    val notify: Boolean,
    val provider: Provider,
    val url: String
) {
    val notes =
        """Session Feedback Submitted for ${referral.contractType} Referral ${referral.ref} with Prime Provider ${provider.name}
            |$url
        """.trimMargin()
}

data class AppointmentReferral(val ref: String, val contractType: String)

enum class Attended {
    YES, LATE, NO;

    companion object {
        fun of(value: String): Attended = values().first { it.name.lowercase() == value.lowercase() }
    }
}
