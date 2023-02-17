package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.randm.ReferAndMonitorClient
import uk.gov.justice.digital.hmpps.integrations.randm.ReferralSession
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.InitialAppointmentSubmitted
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.SessionAppointmentSubmitted
import uk.gov.justice.digital.hmpps.messaging.EventProcessingResult.Failure
import uk.gov.justice.digital.hmpps.service.AppointmentService
import java.net.URI

@Component
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
        val appointment = ramClient.getSession(URI(event.detailUrl!!))?.appointmentOutcome(
            event.personReference.findCrn()!!, event.referralId(), event.contractType()
        )
        if (appointment == null) {
            Failure(IllegalArgumentException("Unable to retrieve session: ${event.detailUrl}"))
        } else {
            appointmentService.updateOutcome(appointment)
            EventProcessingResult.Success(
                SessionAppointmentSubmitted,
                mapOf(
                    "appointmentId" to appointment.id.toString(),
                    "crn" to appointment.crn,
                    "referralId" to appointment.referral.id
                )
            )
        }
    }
}

private fun HmppsDomainEvent.contractType(): ContractType {
    val map = additionalInformation["contractType"] as Map<String, String>
    return ContractType(map["code"]!!, map["name"]!!)
}

private fun HmppsDomainEvent.referralId() = additionalInformation["referralId"] as String

private fun ReferralSession.appointmentOutcome(crn: String, referralId: String, contractType: ContractType) =
    UpdateAppointmentOutcome(
        appointmentId,
        crn,
        Referral(referralId, contractType),
        Attended.of(sessionFeedback.attendance.attended),
        sessionFeedback.behaviour.notifyProbationPractitioner
    )

data class UpdateAppointmentOutcome(
    val id: Long,
    val crn: String,
    val referral: Referral,
    val attended: Attended,
    val notify: Boolean
)

data class Referral(
    val id: String,
    val contractType: ContractType
)

data class ContractType(
    val code: String,
    val name: String
)

enum class Attended {
    YES, LATE, NO;

    companion object {
        fun of(value: String): Attended = values().first { it.name.lowercase() == value.lowercase() }
    }
}
