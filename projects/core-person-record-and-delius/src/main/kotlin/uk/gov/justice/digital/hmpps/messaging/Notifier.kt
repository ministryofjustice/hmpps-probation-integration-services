package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Subscribe
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonAddress
import uk.gov.justice.digital.hmpps.message.*
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher

@Service
@Channel("hmpps-domain-events-topic")
class Notifier(private val topicPublisher: NotificationPublisher) {
    enum class EventType(val type: String, val description: String) {
        ADDRESS_CREATED("probation-case.address.created", "A new address has been created on the probation case"),
        ADDRESS_UPDATED("probation-case.address.updated", "An address has been updated on the probation case"),
        ADDRESS_DELETED("probation-case.address.deleted", "An address has been deleted from the probation case"),
    }

    fun addressCreated(crn: String, cprAddressId: String, address: PersonAddress) {
        EventType.ADDRESS_CREATED.publish(crn, cprAddressId, address)
    }

    fun addressUpdated(crn: String, cprAddressId: String, address: PersonAddress) {
        EventType.ADDRESS_UPDATED.publish(crn, cprAddressId, address)
    }

    fun addressDeleted(crn: String, cprAddressId: String, address: PersonAddress) {
        EventType.ADDRESS_DELETED.publish(crn, cprAddressId, address)
    }

    @Subscribe(
        messages = [
            Message(title = "probation-case.address.created", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "probation-case.address.updated", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "probation-case.address.deleted", payload = Schema(HmppsDomainEvent::class)),
        ]
    )
    fun EventType.publish(crn: String, cprAddressId: String, address: PersonAddress) =
        topicPublisher.publish(
            Notification(
                message = HmppsDomainEvent(
                    version = 1,
                    eventType = type,
                    description = description,
                    personReference = PersonReference(identifiers = listOf(PersonIdentifier("CRN", crn))),
                    additionalInformation = mapOf(
                        "addressStatus" to address.status.code,
                        "addressId" to address.id,
                        "corePersonAddressId" to cprAddressId,
                    )
                ),
                attributes = MessageAttributes(type).apply {
                    set("eventSource", "core-person-record")
                }
            )
        )
}