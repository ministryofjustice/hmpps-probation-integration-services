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
    private val telemetryService: TelemetryService,
    private val notifier: Notifier,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(
        messages = [
            Message(title = ADDRESS_CREATED),
            Message(title = ADDRESS_UPDATED),
            Message(title = ADDRESS_DELETED)
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) = with(notification.message) {
        telemetryService.notificationReceived(notification)
        when (notification.eventType) {
            ADDRESS_CREATED -> addressService.createAddress(crn, cprId)
                .also { notifier.addressCreated(crn, cprId, it) }

            ADDRESS_UPDATED -> addressService.updateAddress(crn, cprId, deliusId)
                .also { notifier.addressUpdated(crn, cprId, it) }

            ADDRESS_DELETED -> addressService.deleteAddress(crn, cprId, deliusId)
                .also { notifier.addressDeleted(crn, cprId, it) }
        }
    }

    private val HmppsDomainEvent.crn get() = requireNotNull(personReference.findCrn()) { "Missing CRN" }
    private val HmppsDomainEvent.cprId get() = requireNotNull(additionalInformation["cprAddressId"]) { "Missing CPR ID" } as String
    private val HmppsDomainEvent.deliusId get() = (requireNotNull(additionalInformation["deliusAddressId"]) { "Missing Delius ID" } as Number).toLong()

    companion object {
        const val ADDRESS_CREATED = "core-person-record.probation.address.created"
        const val ADDRESS_UPDATED = "core-person-record.probation.address.updated"
        const val ADDRESS_DELETED = "core-person-record.probation.address.deleted"
    }
}
