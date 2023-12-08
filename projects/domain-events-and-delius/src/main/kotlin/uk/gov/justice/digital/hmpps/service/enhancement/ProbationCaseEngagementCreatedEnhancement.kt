package uk.gov.justice.digital.hmpps.service.enhancement

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification

@Component
class ProbationCaseEngagementCreatedEnhancement(
    @Value("\${domain-events.base-url}") private val baseUrl: String,
) : Enhancement {
    override val eventType = EnhancedEventType.ProbationCaseEngagementCreated

    override fun enhance(notification: Notification<HmppsDomainEvent>): Notification<HmppsDomainEvent> {
        val message = notification.message
        return notification.copy(
            message =
                message.copy(
                    detailUrl = "$baseUrl/${eventType.value}/${message.personReference.findCrn()}",
                ),
        )
    }
}
