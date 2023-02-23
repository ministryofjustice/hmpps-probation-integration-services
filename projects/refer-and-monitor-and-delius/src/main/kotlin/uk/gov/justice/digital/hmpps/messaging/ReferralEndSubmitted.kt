package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.randm.ReferAndMonitorClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.ReferralEnded
import uk.gov.justice.digital.hmpps.messaging.EventProcessingResult.Success
import uk.gov.justice.digital.hmpps.service.NsiService
import java.net.URI
import java.time.ZonedDateTime

@Component
class ReferralEndSubmitted(
    private val ramClient: ReferAndMonitorClient,
    private val nsiService: NsiService
) : DomainEventHandler {
    override val handledEvents = mapOf(
        ReferralEnded as DomainEventType to ::referralEnded
    )

    @Transactional
    fun referralEnded(event: HmppsDomainEvent): EventProcessingResult = handle {
        val sentReferral = ramClient.getReferral(URI(event.detailUrl!!))
            ?: throw NotFoundException("Unable to retrieve session: ${event.detailUrl}")

        val termination = NsiTermination(
            event.personReference.findCrn()!!,
            event.referralUrn(),
            sentReferral.endDate,
            ReferralEndType.valueOf(event.deliveryState()),
            sentReferral.notes(event.referralUiUrl())
        )
        nsiService.terminateNsi(termination)

        Success(
            ReferralEnded,
            mapOf(
                "crn" to event.personReference.findCrn()!!,
                "referralId" to event.referralId()
            )
        )
    }
}

fun HmppsDomainEvent.deliveryState() = additionalInformation["deliveryState"] as String
fun HmppsDomainEvent.referralUrn() = additionalInformation["referralURN"] as String

fun HmppsDomainEvent.referralId() = additionalInformation["referralId"] as String
fun HmppsDomainEvent.referralUiUrl() = additionalInformation["referralProbationUserURL"] as String

data class SentReferral(
    val referenceNumber: String,
    val referral: Referral,
    val endRequestedAt: ZonedDateTime,
    val concludedAt: ZonedDateTime?
) {
    val endDate = concludedAt ?: endRequestedAt
    fun notes(uiUrl: String) =
        """Referral Ended for ${referral.contractType} Referral $referenceNumber with Prime Provider ${referral.provider.name}
            |$uiUrl
        """.trimMargin()
}

data class Provider(
    val name: String
)

data class Referral(
    val id: String,
    @JsonAlias("serviceProvider")
    val provider: Provider,
    @JsonAlias("contractTypeName")
    val contractType: String
)

enum class ReferralEndType(val outcome: String) {
    CANCELLED("CRS01"),
    PREMATURELY_ENDED("CRS02"),
    COMPLETED("CRS03")
}

data class NsiTermination(
    val crn: String,
    val urn: String,
    val endDate: ZonedDateTime,
    val endType: ReferralEndType,
    val notes: String
)
