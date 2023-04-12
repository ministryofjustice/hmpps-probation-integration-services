package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.randm.ReferAndMonitorClient
import uk.gov.justice.digital.hmpps.integrations.randm.ReferralSession
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.EventProcessingResult.Failure
import uk.gov.justice.digital.hmpps.service.AppointmentService
import uk.gov.justice.digital.hmpps.service.Attended
import uk.gov.justice.digital.hmpps.service.UpdateAppointmentOutcome
import java.net.URI

@Component
class FeedbackSubmitted(
    private val ramClient: ReferAndMonitorClient,
    private val appointmentService: AppointmentService
) : DomainEventHandler {
    override val handledEvents = mapOf(
        DomainEventType.SessionAppointmentSubmitted as DomainEventType to ::sessionAppointmentSubmitted
    )

    fun sessionAppointmentSubmitted(event: HmppsDomainEvent): EventProcessingResult = handle(event) {
        val appointment = ramClient.getSession(URI(event.detailUrl!!))?.appointmentOutcome(
            event.personReference.findCrn()!!,
            event.referralReference(),
            event.contractType(),
            event.providerName(),
            event.url()
        )
        if (appointment == null) {
            Failure(IllegalArgumentException("Unable to retrieve session: ${event.detailUrl}"))
        } else {
            appointmentService.updateOutcome(appointment)
            EventProcessingResult.Success(
                DomainEventType.SessionAppointmentSubmitted,
                mapOf(
                    "appointmentId" to appointment.id.toString(),
                    "crn" to appointment.crn,
                    "referralReference" to appointment.referralReference
                )
            )
        }
    }
}

private fun HmppsDomainEvent.contractType() = additionalInformation["contractTypeName"] as String
private fun HmppsDomainEvent.referralReference() = additionalInformation["referralReference"] as String
private fun HmppsDomainEvent.providerName() = additionalInformation["primeProviderName"] as String
private fun HmppsDomainEvent.url() = additionalInformation["referralProbationUserURL"] as String

private fun ReferralSession.appointmentOutcome(
    crn: String,
    referralReference: String,
    contractType: String,
    providerName: String,
    url: String
) = UpdateAppointmentOutcome(
    appointmentId,
    crn,
    referralReference,
    Referral("", Provider(providerName), contractType),
    Attended.of(sessionFeedback.attendance.attended),
    sessionFeedback.behaviour.notifyProbationPractitioner ?: false,
    url
)
