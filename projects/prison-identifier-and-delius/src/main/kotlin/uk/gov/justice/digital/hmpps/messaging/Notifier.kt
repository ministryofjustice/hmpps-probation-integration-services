package uk.gov.justice.digital.hmpps.messaging

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.message.*
import uk.gov.justice.digital.hmpps.model.PrisonIdentifiers
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import kotlin.streams.asSequence

@Service
class Notifier(
    private val personRepository: PersonRepository,
    @Qualifier("queuePublisher") private val queuePublisher: NotificationPublisher,
    @Qualifier("topicPublisher") private val topicPublisher: NotificationPublisher,
) {
    companion object {
        const val REQUEST_PRISON_MATCH = "prison-identifier.internal.prison-match-requested"
        const val REQUEST_PROBATION_MATCH = "prison-identifier.internal.probation-match-requested"
    }

    @Async
    @Transactional
    fun requestPrisonMatchingAsync(crns: List<String>, dryRun: Boolean) {
        requestPrisonMatching(crns, dryRun)
    }

    @Transactional
    fun requestPrisonMatching(crns: List<String>, dryRun: Boolean) {
        crns.asSequence()
            .ifEmpty { personRepository.findAllCrns().asSequence() }
            .map { notification(REQUEST_PRISON_MATCH, PersonIdentifier("CRN", it), dryRun) }
            .forEach { queuePublisher.publish(it) }
    }

    @Async
    fun requestProbationMatchingAsync(nomsNumbers: List<String>, dryRun: Boolean) {
        nomsNumbers.asSequence()
            .map { notification(REQUEST_PROBATION_MATCH, PersonIdentifier("NOMS", it), dryRun) }
            .forEach { queuePublisher.publish(it) }
    }

    fun identifierAdded(crn: String, prisonIdentifiers: PrisonIdentifiers) {
        topicPublisher.publish(
            Notification(
                message = HmppsDomainEvent(
                    version = 1,
                    eventType = "probation-case.prison-identifier.added",
                    description = "A probation case has been matched with a booking in the prison system. The prisoner and booking identifiers have been added to the probation case.",
                    personReference = PersonReference(
                        identifiers = listOf(
                            PersonIdentifier("CRN", crn),
                            PersonIdentifier("NOMS", prisonIdentifiers.prisonerNumber),
                        ),
                    ),
                    nullableAdditionalInformation = prisonIdentifiers.bookingNumber?.let {
                        AdditionalInformation(
                            info = mutableMapOf(
                                "bookingNumber" to it
                            )
                        )
                    }
                ),
                attributes = MessageAttributes("probation-case.prison-identifier.added")
            )
        )
    }

    fun identifierUpdated(crn: String, nomsNumber: String, previousNomsNumber: String) {
        topicPublisher.publish(
            Notification(
                message = HmppsDomainEvent(
                    version = 1,
                    eventType = "probation-case.prison-identifier.updated",
                    description = "A prisoner identifier has been updated following a merge. This been reflected on the probation case.",
                    personReference = PersonReference(
                        identifiers = listOf(
                            PersonIdentifier("CRN", crn),
                            PersonIdentifier("NOMS", nomsNumber),
                        ),
                    ),
                    nullableAdditionalInformation = AdditionalInformation(info = mutableMapOf("previousNomsNumber" to previousNomsNumber)),
                ),
                attributes = MessageAttributes("probation-case.prison-identifier.updated")
            )
        )
    }

    private fun notification(eventType: String, identifier: PersonIdentifier, dryRun: Boolean) =
        Notification(
            message = HmppsDomainEvent(
                eventType = eventType,
                version = 1,
                nullableAdditionalInformation = AdditionalInformation(mutableMapOf("dryRun" to dryRun)),
                personReference = PersonReference(listOf(identifier))
            ),
            attributes = MessageAttributes(eventType)
        )
}