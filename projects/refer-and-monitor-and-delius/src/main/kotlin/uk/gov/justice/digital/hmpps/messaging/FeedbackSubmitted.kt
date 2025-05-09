package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.security.nullIfNotFound
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.integrations.randm.ReferralSession
import uk.gov.justice.digital.hmpps.integrations.randm.SupplierAssessment
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.EventProcessingResult.Failure
import uk.gov.justice.digital.hmpps.service.AppointmentService
import uk.gov.justice.digital.hmpps.service.Attended
import uk.gov.justice.digital.hmpps.service.Outcome
import uk.gov.justice.digital.hmpps.service.UpdateAppointmentOutcome

@Component
class FeedbackSubmitted(
    private val detailService: DomainEventDetailService,
    private val appointmentService: AppointmentService
) : DomainEventHandler {
    override val handledEvents = mapOf(
        DomainEventType.InitialAppointmentSubmitted to ::initialAppointmentSubmitted,
        DomainEventType.SessionAppointmentSubmitted to ::sessionAppointmentSubmitted
    )

    fun initialAppointmentSubmitted(event: HmppsDomainEvent): EventProcessingResult = handle(event) {
        val appointment = nullIfNotFound { detailService.getDetail<SupplierAssessment>(event) }?.appointmentOutcome(
            event.personReference.findCrn()!!,
            event.referralReference(),
            event.contractType(),
            event.providerName(),
            event.url(),
            event.deliusId()
        )
        updateAppointment(appointment, event)
    }

    fun sessionAppointmentSubmitted(event: HmppsDomainEvent): EventProcessingResult = handle(event) {
        val appointment = nullIfNotFound { detailService.getDetail<ReferralSession>(event) }?.appointmentOutcome(
            event.personReference.findCrn()!!,
            event.referralId(),
            event.referralReference(),
            event.contractType(),
            event.providerName(),
            event.url()
        )
        updateAppointment(appointment, event)
    }

    private fun updateAppointment(appointment: UpdateAppointmentOutcome?, event: HmppsDomainEvent) =
        if (appointment == null) {
            Failure(IllegalArgumentException("Unable to retrieve appointment: ${event.detailUrl}"))
        } else {
            appointmentService.updateOutcome(appointment)
            EventProcessingResult.Success(
                DomainEventType.of(event.eventType),
                mapOf(
                    "appointmentId" to appointment.id.toString(),
                    "crn" to appointment.crn,
                    "referralReference" to appointment.referralReference
                )
            )
        }
}

private fun HmppsDomainEvent.contractType() = additionalInformation["contractTypeName"] as String
private fun HmppsDomainEvent.referralId() = additionalInformation["referralId"] as String
private fun HmppsDomainEvent.referralReference() = additionalInformation["referralReference"] as String
private fun HmppsDomainEvent.providerName() = additionalInformation["primeProviderName"] as String
private fun HmppsDomainEvent.url() = additionalInformation["referralProbationUserURL"] as String
private fun HmppsDomainEvent.deliusId() = (additionalInformation["deliusAppointmentId"] as String?)?.toLong()

private fun ReferralSession.appointmentOutcome(
    crn: String,
    referralId: String,
    referralReference: String,
    contractType: String,
    providerName: String,
    url: String
): UpdateAppointmentOutcome {
    val feedback = checkNotNull(latestFeedback?.appointmentFeedback) {
        "No feedback information available for referral $referralId : session $id"
    }
    val attended = Attended.of(feedback.attendanceFeedback.attended!!)
    return UpdateAppointmentOutcome(
        latestFeedback!!.id,
        deliusId,
        crn,
        referralReference,
        Referral(referralId, Provider(providerName), contractType),
        Outcome(
            attended,
            didSessionHappen = feedback.attendanceFeedback.didSessionHappen,
            noSessionReasonType = feedback.sessionFeedback.noSessionReasonType,
            notify = feedback.sessionFeedback.notifyProbationPractitioner ?: true
        ),
        url
    )
}

private fun SupplierAssessment.appointmentOutcome(
    crn: String,
    referralReference: String,
    contractType: String,
    providerName: String,
    url: String,
    deliusId: Long?
): UpdateAppointmentOutcome {
    val feedback = checkNotNull(latestFeedback?.appointmentFeedback) {
        "No feedback information available for referral $referralId: supplier assessment $id"
    }
    val attended = Attended.of(feedback.attendanceFeedback.attended!!)
    return UpdateAppointmentOutcome(
        latestFeedback!!.id,
        deliusId,
        crn,
        referralReference,
        Referral(referralId.toString(), Provider(providerName), contractType),
        Outcome(
            attended,
            didSessionHappen = feedback.attendanceFeedback.didSessionHappen,
            noSessionReasonType = feedback.sessionFeedback.noSessionReasonType,
            notify = feedback.sessionFeedback.notifyProbationPractitioner ?: true
        ),
        url
    )
}
