package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.AddressService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("core-person-record-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val addressService: AddressService,
    private val telemetryService: TelemetryService
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(title = ADDRESS_CREATED),
            Message(title = ADDRESS_UPDATED),
            Message(title = ADDRESS_DELETED)
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val crn = requireNotNull(notification.message.personReference.findCrn()) { "Missing CRN" }
        val id = requireNotNull(notification.message.additionalInformation["cprAddressId"]) { "Missing ID" } as String
        when (notification.eventType) {
            ADDRESS_CREATED -> addressService.createAddress(crn, id)
            ADDRESS_UPDATED -> addressService.updateAddress(crn, id)
            ADDRESS_DELETED -> addressService.deleteAddress(crn, id)
        }
    }

    companion object {
        const val ADDRESS_CREATED = "core-person-record.probation.address.created"
        const val ADDRESS_UPDATED = "core-person-record.probation.address.updated"
        const val ADDRESS_DELETED = "core-person-record.probation.address.deleted"
    }
}
