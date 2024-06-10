package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.stereotype.Component
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

    fun referralEnded(event: HmppsDomainEvent): EventProcessingResult = handle(event) {
        val sentReferral = ramClient.getReferral(URI(event.detailUrl!!))
            ?: throw NotFoundException("Unable to retrieve session: ${event.detailUrl}")

        val termination = NsiTermination(
            event.personReference.findCrn()!!,
            event.referralUrn(),
            sentReferral.relevantSentenceId,
            sentReferral.sentAt,
            sentReferral.endDate
                ?: throw IllegalStateException("No End Date for Termination: ${event.referralUrn()} => ${event.personReference.findCrn()}"),
            ReferralEndType.valueOf(event.deliveryState()),
            event.withdrawalCode(),
            sentReferral.notes(event.referralUiUrl()),
            sentReferral.endOfServiceReport?.submittedAt,
            sentReferral.notificationNotes(event.referralUiUrl())
        )
        nsiService.terminateNsi(termination)

        Success(
            ReferralEnded,
            mapOf(
                "crn" to event.personReference.findCrn()!!,
                "referralUrn" to event.referralUrn(),
                "endDate" to sentReferral.endDate.toString(),
                "endType" to termination.endType.toString(),
                "withdrawalCode" to termination.withdrawalCode
            )
        )
    }
}

fun HmppsDomainEvent.deliveryState() = additionalInformation["deliveryState"] as String
fun HmppsDomainEvent.withdrawalCode() = additionalInformation["withdrawalCode"] as String
fun HmppsDomainEvent.referralUrn() = additionalInformation["referralURN"] as String
fun HmppsDomainEvent.referralUiUrl() = additionalInformation["referralProbationUserURL"] as String

data class SentReferral(
    val referenceNumber: String,
    val relevantSentenceId: Long,
    val referral: Referral,
    val sentAt: ZonedDateTime,
    val endRequestedAt: ZonedDateTime?,
    val concludedAt: ZonedDateTime?,
    val endOfServiceReport: EndOfServiceReport?
) {
    val endDate = concludedAt ?: endRequestedAt
    fun notes(uiUrl: String) =
        """Referral Ended for ${referral.contractType} Referral $referenceNumber with Prime Provider ${referral.provider.name}
            |$uiUrl
        """.trimMargin()

    fun notificationNotes(uiUrl: String) =
        """End of Service Report Submitted for ${referral.contractType} Referral $referenceNumber with Prime Provider ${referral.provider.name}
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
) {
    val urn = "urn:hmpps:interventions-referral:$id"
}

enum class ReferralEndType(val outcome: String) {
    CANCELLED("CRS01"),
    PREMATURELY_ENDED("CRS02"),
    COMPLETED("CRS03")
}

data class EndOfServiceReport(val submittedAt: ZonedDateTime?)

data class NsiTermination(
    val crn: String,
    val urn: String,
    val eventId: Long,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val endType: ReferralEndType,
    val withdrawalCode: String,
    val notes: String,
    val notificationDateTime: ZonedDateTime?,
    val notificationNotes: String
)
