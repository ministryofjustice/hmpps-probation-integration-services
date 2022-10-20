package uk.gov.justice.digital.hmpps.listener

import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.integrations.delius.RiskScoreService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
@EnableJms
class MessageListener(
    private val telemetryService: TelemetryService,
    private val riskScoreService: RiskScoreService,
) {
    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val hmppsEvent = notification.message
        when (hmppsEvent.eventType) {
            "risk-assessment.scores.rsr.determined" -> {
                riskScoreService.updateRsrScores(
                    hmppsEvent.personReference.findCrn() ?: throw IllegalArgumentException("Missing CRN in ${hmppsEvent.personReference}"),
                    hmppsEvent.additionalInformation["EventNumber"] as Int,
                    hmppsEvent.assessmentDate(),
                    hmppsEvent.rsr(),
                    hmppsEvent.ospIndecent(),
                    hmppsEvent.ospContact()
                )
                telemetryService.trackEvent("RsrScoresUpdated", hmppsEvent.telemetryProperties())
            }
            "risk-assessment.scores.ogrs.determined" ->
                telemetryService.trackEvent("UnsupportedEventType", hmppsEvent.telemetryProperties())
            else -> throw IllegalArgumentException("Unexpected event type ${notification.eventType}")
        }
    }
}

fun HmppsDomainEvent.assessmentDate() =
    ZonedDateTimeDeserializer.deserialize(additionalInformation["AssessmentDate"] as String)

data class RiskAssessment(val score: Double, val band: String)
fun HmppsDomainEvent.rsr() = RiskAssessment(
    additionalInformation["RSRScore"] as Double,
    additionalInformation["RSRBand"] as String
)
fun HmppsDomainEvent.ospIndecent() = RiskAssessment(
    additionalInformation["OSPIndecentScore"] as Double,
    additionalInformation["OSPIndecentBand"] as String
)
fun HmppsDomainEvent.ospContact() = RiskAssessment(
    additionalInformation["OSPContactScore"] as Double,
    additionalInformation["OSPContactBand"] as String
)

fun HmppsDomainEvent.telemetryProperties() = mapOf(
    "occurredAt" to occurredAt.toString(),
    "personReference" to personReference.toString(),
    "additionalInformation" to additionalInformation.toString(),
)
