package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Subscribe
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
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
    @Qualifier("topicPublisher") private val topicPublisher: NotificationPublisher,
) {
    companion object {
        const val BULK_KEY_DATE_UPDATE = "custody-key-dates.internal.bulk-update"
        const val PROBATION_KEY_DATE_UPDATE = "probation-case.custody-key-dates.updated"
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Transactional
    @Subscribe(messages = [Message(title = BULK_KEY_DATE_UPDATE, payload = Schema(HmppsDomainEvent::class))])
    fun requestBulkUpdate(nomsIds: List<String>, dryRun: Boolean) {
        var count = 0
        nomsIds.asSequence()
            .ifEmpty { personRepository.findNomsSingleCustodial().asSequence() }
            .map { notification(PersonIdentifier("NOMS", it), dryRun) }
            .forEach {
                queuePublisher.publish(it)
                count++
            }
        log.info("Published $count messages successfully")
    }

    fun publishChange(nomisId: String) {
        topicPublisher.publish(
            Notification(
                message = HmppsDomainEvent(
                    eventType = PROBATION_KEY_DATE_UPDATE,
                    description = "Probation case updated with custody key dates",
                    version = 1,
                    personReference = PersonReference(
                        listOfNotNull(
                            PersonIdentifier("NOMS", nomisId),
                            personRepository.findCrnByNomisId(nomisId)?.let { PersonIdentifier("CRN", it) }
                        )
                    )
                ),
                attributes = MessageAttributes(PROBATION_KEY_DATE_UPDATE)
            )
        )
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