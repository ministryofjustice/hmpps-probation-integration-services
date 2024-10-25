package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Subscribe
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress
import uk.gov.justice.digital.hmpps.message.*
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher

@Service
@Channel("hmpps-domain-events-topic")
class Notifier(
    @Qualifier("topicPublisher") private val topicPublisher: NotificationPublisher,
) {
    @Subscribe(
        messages = [
            Message(title = "probation-case.engagement.created", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "probation-case.address.created", payload = Schema(HmppsDomainEvent::class))
        ]
    )
    fun caseCreated(person: Person) {
        topicPublisher.publish(
            Notification(
                message = HmppsDomainEvent(
                    version = 1,
                    eventType = "probation-case.engagement.created",
                    description = "A probation case record for a person has been created in Delius",
                    personReference = PersonReference(
                        identifiers = listOf(
                            PersonIdentifier("CRN", person.crn),
                        ),
                    ),
                ),
                attributes = MessageAttributes("probation-case.engagement.created")
            )
        )
    }

    fun addressCreated(personAddress: PersonAddress) {
        topicPublisher.publish(
            Notification(
                message = HmppsDomainEvent(
                    version = 1,
                    eventType = "probation-case.address.created",
                    description = "A new address has been created on the probation case",
                    personReference = PersonReference(
                        identifiers = listOf(
                            PersonIdentifier("CRN", personAddress.person.crn),
                        ),
                    ),
                    additionalInformation = mapOf(
                        "addressStatus" to personAddress.status.description,
                        "addressId" to personAddress.id.toString()
                    )
                ),
                attributes = MessageAttributes("probation-case.address.created")
            )
        )
    }
}