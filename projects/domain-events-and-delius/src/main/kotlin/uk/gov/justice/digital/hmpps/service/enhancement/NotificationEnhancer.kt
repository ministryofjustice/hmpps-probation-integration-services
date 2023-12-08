package uk.gov.justice.digital.hmpps.service.enhancement

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification

@Component
class NotificationEnhancer(enhancements: List<Enhancement>) {
    val enhancements = enhancements.associateBy { it.eventType }

    fun enhance(notification: Notification<HmppsDomainEvent>): Notification<HmppsDomainEvent> = getEnhancement(EnhancedEventType.of(notification.message.eventType)).enhance(notification)

    private fun getEnhancement(enhancedEventType: EnhancedEventType): Enhancement =
        enhancements[enhancedEventType] ?: object : Enhancement {
            override val eventType = enhancedEventType

            override fun enhance(notification: Notification<HmppsDomainEvent>) = notification
        }
}
