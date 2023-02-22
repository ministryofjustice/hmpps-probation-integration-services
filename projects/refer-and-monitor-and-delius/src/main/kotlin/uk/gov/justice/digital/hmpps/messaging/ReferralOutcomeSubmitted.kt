package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.ReferralEnded
import uk.gov.justice.digital.hmpps.service.NsiService
import java.time.ZonedDateTime

@Component
class ReferralOutcomeSubmitted(
    private val nsiService: NsiService
) : DomainEventHandler {
    override val handledEvents = mapOf(
        ReferralEnded as DomainEventType to ::referralEnded
    )

    @Transactional
    fun referralEnded(event: HmppsDomainEvent): EventProcessingResult {
        TODO()
    }
}

data class NsiTermination(val crn: String, val urn: String, val endDate: ZonedDateTime, val endType: ReferralEndType)
enum class ReferralEndType(val outcome: String) {
    CANCELLED("CRS01"),
    PREMATURELY_ENDED("CRS02"),
    COMPLETED("CRS03")
}