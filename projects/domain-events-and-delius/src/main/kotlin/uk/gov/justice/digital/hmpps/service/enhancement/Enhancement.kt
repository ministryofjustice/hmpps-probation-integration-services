package uk.gov.justice.digital.hmpps.service.enhancement

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification

interface Enhancement {
    val eventType: EnhancedEventType
    fun enhance(notification: Notification<HmppsDomainEvent>): Notification<HmppsDomainEvent>
}

sealed interface EnhancedEventType {
    val value: String

    data object ProbationCaseEngagementCreated : EnhancedEventType {
        override val value = "probation-case.engagement.created"
    }

    private data class NoEnhancement(override val value: String) : EnhancedEventType

    companion object {
        private val types = listOf(
            ProbationCaseEngagementCreated
        ).associateBy { it.value }

        fun of(value: String): EnhancedEventType = types[value] ?: NoEnhancement(value)
    }
}