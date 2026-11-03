package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.model.DutyToRefer
import uk.gov.justice.digital.hmpps.service.ContactService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("single-accommodation-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val detailService: DomainEventDetailService,
    private val contactService: ContactService,
    private val telemetryService: TelemetryService,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(messages = [Message(title = DUTY_TO_REFER_CREATED), Message(title = DUTY_TO_REFER_UPDATED)])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val event = notification.message
        val typeCode = when (event.eventType) {
            DUTY_TO_REFER_CREATED -> ContactType.DUTY_TO_REFER_CREATED
            DUTY_TO_REFER_UPDATED -> ContactType.DUTY_TO_REFER_UPDATED
            else -> error("Unexpected event type ${event.eventType}")
        }
        val details = detailService.getDetail<DutyToRefer>(event)
        val contact = contactService.createContact(details, event.occurredAt, typeCode)
        telemetryService.trackEvent(
            "DutyToReferContactCreated",
            mapOf(
                "crn" to details.crn,
                "contactId" to contact.id.toString(),
                "contactType" to contact.type.code,
                "externalReference" to contact.externalReference,
            )
        )
    }

    companion object {
        const val DUTY_TO_REFER_CREATED = "sas.duty-to-refer.created"
        const val DUTY_TO_REFER_UPDATED = "sas.duty-to-refer.updated"
    }
}
