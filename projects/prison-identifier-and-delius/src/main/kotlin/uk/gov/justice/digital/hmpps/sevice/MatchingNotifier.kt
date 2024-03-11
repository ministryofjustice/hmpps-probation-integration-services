package uk.gov.justice.digital.hmpps.sevice

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.message.*
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher

@Service
class MatchingNotifier(
    private val personRepository: PersonRepository,
    private val notificationPublisher: NotificationPublisher
) {
    fun sendForMatch(crns: List<String>, dryRun: Boolean) {
        crns.ifEmpty { personRepository.findAllCrns() }.asSequence()
            .map { notification(it, dryRun) }
            .forEach { notificationPublisher.publish(it) }
    }

    private fun notification(crn: String, dryRun: Boolean): Notification<HmppsDomainEvent> = Notification(
        HmppsDomainEvent(
            INTERNAL_EVENT_TYPE,
            1,
            nullableAdditionalInformation = AdditionalInformation(mutableMapOf("dryRun" to dryRun)),
            personReference = PersonReference(listOf(PersonIdentifier("CRN", crn)))
        ),
        attributes = MessageAttributes(INTERNAL_EVENT_TYPE)
    )

    companion object {
        const val INTERNAL_EVENT_TYPE = "prison-identifier.internal.match"
    }
}