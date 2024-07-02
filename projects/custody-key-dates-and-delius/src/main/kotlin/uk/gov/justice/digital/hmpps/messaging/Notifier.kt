package uk.gov.justice.digital.hmpps.messaging

import jakarta.transaction.Transactional
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.message.*
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import kotlin.streams.asSequence

@Service
@Channel("hmpps-domain-events-topic")
class Notifier(
    private val personRepository: PersonRepository,
    @Qualifier("queuePublisher") private val queuePublisher: NotificationPublisher,
) {
    companion object {
        const val BULK_KEY_DATE_UPDATE = "custody-key-dates.internal.bulk-update"
    }

    @Async
    @Transactional
    @Subscribe(messages = [Message(messageId = BULK_KEY_DATE_UPDATE, payload = Schema(HmppsDomainEvent::class))])
    fun requestBulkUpdate(nomsIds: List<String>, dryRun: Boolean) {
        nomsIds.asSequence()
            .ifEmpty { personRepository.findNomsSingleCustodial().asSequence() }
            .map { notification(PersonIdentifier("NOMS", it), dryRun) }
            .forEach { queuePublisher.publish(it) }
    }

    private fun notification(identifier: PersonIdentifier, dryRun: Boolean) =
        Notification(
            message = HmppsDomainEvent(
                eventType = BULK_KEY_DATE_UPDATE,
                version = 1,
                additionalInformation = mapOf("dryRun" to dryRun),
                personReference = PersonReference(listOf(identifier))
            ),
            attributes = MessageAttributes(BULK_KEY_DATE_UPDATE)
        )
}