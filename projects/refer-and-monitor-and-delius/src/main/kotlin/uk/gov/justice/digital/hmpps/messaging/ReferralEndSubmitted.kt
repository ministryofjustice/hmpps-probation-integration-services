package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.randm.ReferAndMonitorClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.ReferralEnded
import uk.gov.justice.digital.hmpps.messaging.EventProcessingResult.Success
import uk.gov.justice.digital.hmpps.messaging.ReferralWithdrawalNsiOutcome.*
import uk.gov.justice.digital.hmpps.messaging.ReferralWithdrawalState.PRE_ICA_WITHDRAWAL
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
            sentReferral.withdrawalCode?.toOutcome(sentReferral.withdrawalState),
            sentReferral.notes(event.referralUiUrl()),
            sentReferral.endOfServiceReport?.submittedAt,
            sentReferral.notificationNotes(event.referralUiUrl())
        )
        nsiService.terminateNsi(termination)

        Success(
            ReferralEnded,
            listOfNotNull(
                "crn" to event.personReference.findCrn()!!,
                "referralUrn" to event.referralUrn(),
                "endDate" to sentReferral.endDate.toString(),
                "endType" to termination.endType.toString(),
                termination.withdrawalOutcome?.let { "withdrawalOutcome" to it.name },
            ).toMap()
        )
    }
}

fun HmppsDomainEvent.deliveryState() = additionalInformation["deliveryState"] as String
fun HmppsDomainEvent.referralUrn() = additionalInformation["referralURN"] as String
fun HmppsDomainEvent.referralUiUrl() = additionalInformation["referralProbationUserURL"] as String

data class SentReferral(
    val referenceNumber: String,
    val relevantSentenceId: Long,
    val referral: Referral,
    val sentAt: ZonedDateTime,
    val endRequestedAt: ZonedDateTime?,
    val concludedAt: ZonedDateTime?,
    val endOfServiceReport: EndOfServiceReport?,
    val withdrawalState: ReferralWithdrawalState?,
    val withdrawalCode: ReferralWithdrawalReason?,
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

enum class ReferralWithdrawalState {
    PRE_ICA_WITHDRAWAL,
    POST_ICA_WITHDRAWAL,
    POST_ICA_CLOSE_REFERRAL_EARLY,
}

enum class ReferralWithdrawalReason(
    val description: String,
    private val preIcaOutcome: ReferralWithdrawalNsiOutcome,
    private val postIcaOutcome: ReferralWithdrawalNsiOutcome = preIcaOutcome
) {
    // Problem with referral
    INE("Ineligible Referral", NSI1),
    MIS("Mistaken or duplicate referral", NSI2),

    // User related
    NOT("Not engaged", NSI11, NSI12),
    NEE("Needs met through another route", NSI7, NSI8),
    MOV("Moved out of service area", NSI5, NSI6),
    WOR("Work or caring responsibilities", NSI3, NSi4),
    USE("User died", NSI5, NSI6),

    // Sentence / custody related
    ACQ("Acquitted on appeal", NSI5, NSI6),
    RET("Returned to custody", NSI5, NSI6),
    SER("Sentence revoked", NSI5, NSI6),
    SEE("Sentence expired", NSI9, NSI10),
    EAR("Intervention has been completed", NSI13),

    // Other
    ANO("Another reason", CRS01);

    fun toOutcome(withdrawalState: ReferralWithdrawalState?): ReferralWithdrawalNsiOutcome {
        val state = requireNotNull(withdrawalState) { "Withdrawal state not provided" }
        return if (state == PRE_ICA_WITHDRAWAL) preIcaOutcome else postIcaOutcome
    }
}

enum class ReferralWithdrawalNsiOutcome(val description: String) {
    NSI1("Ineligible referral"),
    NSI2("Mistaken referral (inc Duplicate)"),
    NSI3("Did not start (Work, Caring Commitments, Long-term Sickness)"),
    NSi4("Started - Not finished (Work,Caring Commitments or Long-Term Sickness)"), // Note: intentional typo (NSI4 -> NSi4) to match Delius data
    NSI5("Did not start – Changed Circumstances"),
    NSI6("Started - Not finished (Changed Circumstances)"),
    NSI7("Did not start - Needs met through another route"),
    NSI8("Started - not finished (Needs met through another route)"),
    NSI9("Did not start - Sentence Expired"),
    NSI10("Started - Not finished (Sentence Expired)"),
    NSI11("Did not start – Disengaged"),
    NSI12("Started - Not finished (Disengaged)"),
    NSI13("Completed"),
    CRS01("Cancelled"),
}

data class EndOfServiceReport(val submittedAt: ZonedDateTime?)

data class NsiTermination(
    val crn: String,
    val urn: String,
    val eventId: Long,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val endType: ReferralEndType,
    val withdrawalOutcome: ReferralWithdrawalNsiOutcome?,
    val notes: String,
    val notificationDateTime: ZonedDateTime?,
    val notificationNotes: String
)
