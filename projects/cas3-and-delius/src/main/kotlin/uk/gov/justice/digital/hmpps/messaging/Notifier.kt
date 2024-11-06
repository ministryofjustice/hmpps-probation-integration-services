package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Subscribe
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.message.*
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher

@Service
@Channel("hmpps-domain-events-topic")
class Notifier(@Qualifier("topicPublisher") private val topicPublisher: NotificationPublisher) {
    @Subscribe(
        messages = [
            Message(title = "probation-case.address.created", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "probation-case.address.updated", payload = Schema(HmppsDomainEvent::class))
        ]
    )
    fun addressCreated(crn: String, addressId: Long, addressStatus: String) {
        topicPublisher.publish(
            Notification(
                message = HmppsDomainEvent(
                    version = 1,
                    eventType = "probation-case.address.created",
                    description = "A new address has been created on the probation case",
                    personReference = PersonReference(
                        identifiers = listOf(
                            PersonIdentifier("CRN", crn),
                        ),
                    ),
                    additionalInformation = mapOf(
                        "addressStatus" to addressStatus,
                        "addressId" to addressId
                    )
                ),
                attributes = MessageAttributes("probation-case.address.created")
            )
        )
    }

    fun addressUpdated(crn: String, addressId: Long, addressStatus: String) {
        topicPublisher.publish(
            Notification(
                message = HmppsDomainEvent(
                    version = 1,
                    eventType = "probation-case.address.updated",
                    description = "An address has been updated on the probation case",
                    personReference = PersonReference(
                        identifiers = listOf(
                            PersonIdentifier("CRN", crn),
                        ),
                    ),
                    additionalInformation = mapOf(
                        "addressStatus" to addressStatus,
                        "addressId" to addressId
                    )
                ),
                attributes = MessageAttributes("probation-case.address.updated")
            )
        )
    }
}