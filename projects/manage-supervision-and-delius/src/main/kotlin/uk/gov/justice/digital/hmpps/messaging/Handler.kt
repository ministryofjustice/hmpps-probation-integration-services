package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.api.model.contact.CreateContact
import uk.gov.justice.digital.hmpps.api.model.sms.SmsDetail
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.ContactLogService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived

@Component
@Channel("manage-supervision-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val detailService: DomainEventDetailService,
    private val telemetryService: TelemetryService,
    private val contactLogService: ContactLogService,
    private val appointmentRepository: AppointmentRepository,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(name = "probation.appointment.sms-sent-to-pop", payload = Schema(HmppsDomainEvent::class)),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        when (notification.eventType) {
            "probation.appointment.sms-sent-to-pop" -> {
                val crn = notification.message.personReference.findCrn().orNotFoundBy("id", notification.id)
                val smsDetails = detailService.getDetail<SmsDetail>(notification.message)
                val relatedAppointment = appointmentRepository.findAppointmentByPerson_CrnAndExternalReference(
                    crn = crn,
                    externalReference = smsDetails.deliusExternalReference
                ).orNotFoundBy("appointmentExternalReference", smsDetails.deliusExternalReference)
                contactLogService.createContact(
                    crn = crn,
                    createContact = CreateContact(
                        date = notification.message.occurredAt.toLocalDate(),
                        time = notification.message.occurredAt.toLocalTime(),
                        type = CreateContact.Type.EmailTextToPoP.code,
                        staffCode = relatedAppointment.staff.code,
                        teamCode = relatedAppointment.team.code,
                        notes = "SMS sent to MPOP with content: ${smsDetails.smsMessage}"
                    )
                )
                telemetryService.trackEvent("SmsContactCreated", notification.message.telemetry())
            }
        }
    }
}

val HmppsDomainEvent.id get() = additionalInformation["applicationId"] as String?

fun HmppsDomainEvent.telemetry() = mapOf(
    "crn" to personReference.findCrn(),
    "id" to id,
)
