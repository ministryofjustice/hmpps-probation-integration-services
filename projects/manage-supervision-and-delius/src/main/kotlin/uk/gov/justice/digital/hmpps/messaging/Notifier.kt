package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Subscribe
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonAddress
import uk.gov.justice.digital.hmpps.message.*
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher

@Service
@Channel("hmpps-domain-events-topic")
class Notifier(
    @Qualifier("topicPublisher") private val topicPublisher: NotificationPublisher,
) {
    @Subscribe(
        messages = [
            Message(title = "probation-case.address.created", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "probation-case.address.updated", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "probation-case.personal-details.updated", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "probation-case.supervision.created", payload = Schema(HmppsDomainEvent::class))
        ]
    )

    fun addressUpdated(personAddress: PersonAddress, crn: String) {
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
                        "addressStatus" to personAddress.status.description,
                        "addressId" to personAddress.id.toString()
                    )
                ),
                attributes = MessageAttributes("probation-case.address.updated")
            )
        )
    }

    fun addressCreated(personAddress: PersonAddress, crn: String) {
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
                        "addressStatus" to personAddress.status.description,
                        "addressId" to personAddress.id.toString()
                    )
                ),
                attributes = MessageAttributes("probation-case.address.created")
            )
        )
    }

    fun caseUpdated(person: Person) {
        topicPublisher.publish(
            Notification(
                message = HmppsDomainEvent(
                    version = 1,
                    eventType = "probation-case.personal-details.updated",
                    description = "A probation case record for a person has been updated in Delius",
                    personReference = PersonReference(
                        identifiers = listOf(
                            PersonIdentifier("CRN", person.crn),
                        ),
                    ),
                ),
                attributes = MessageAttributes("probation-case.personal-details.updated")
            )
        )
    }

    fun contactCreated(contactId: Long, isVisor: Boolean, crn: String) {
        topicPublisher.publish(
            Notification(
                message = HmppsDomainEvent(
                    version = 1,
                    eventType = "probation-case.supervision.created",
                    description = "A supervision contact has been created in NDelius",
                    personReference = PersonReference(
                        identifiers = listOf(PersonIdentifier("CRN", crn)),
                    ),
                    additionalInformation = mapOf(
                        "contactId" to contactId,
                        "mapps" to mapOf(
                            "export" to isVisor
                        )
                    )
                ),
                attributes = MessageAttributes("probation-case.supervision.created")
            )
        )
    }
}