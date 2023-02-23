package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.databind.JsonMappingException
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.integrations.delius.DeliusValidationError
import uk.gov.justice.digital.hmpps.integrations.delius.RiskAssessmentService
import uk.gov.justice.digital.hmpps.integrations.delius.RiskScoreService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived

@Component
class Handler(
    private val telemetryService: TelemetryService,
    private val riskScoreService: RiskScoreService,
    private val riskAssessmentService: RiskAssessmentService,
    override val converter: NotificationConverter<HmppsDomainEvent>
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val message = notification.message
        try {
            when (message.eventType) {
                "risk-assessment.scores.rsr.determined" -> {
                    riskScoreService.updateRsrScores(
                        message.personReference.findCrn()
                            ?: throw IllegalArgumentException("Missing CRN in ${message.personReference}"),
                        message.additionalInformation["EventNumber"] as Int?,
                        message.assessmentDate(),
                        message.rsr(),
                        message.ospIndecent(),
                        message.ospContact()
                    )
                    telemetryService.trackEvent("RsrScoresUpdated", message.telemetryProperties())
                }

                "risk-assessment.scores.ogrs.determined" -> {
                    // if the message doesn't contain the event number then the event is coming from the prison side
                    // so ignore the message
                    if (!message.additionalInformation.containsKey("EventNumber")) {
                        telemetryService.trackEvent("AddOrUpdateRiskAssessment - ignored due to no event number", message.telemetryProperties())
                        return
                    }
                    riskAssessmentService.addOrUpdateRiskAssessment(
                        message.personReference.findCrn()
                            ?: throw IllegalArgumentException("Missing CRN in ${message.personReference}"),
                        message.additionalInformation["EventNumber"] as Int?,
                        message.assessmentDate(),
                        message.ogrsScore()
                    )
                    telemetryService.trackEvent("AddOrUpdateRiskAssessment", message.telemetryProperties())
                }

                else -> throw IllegalArgumentException("Unexpected event type ${message.eventType}")
            }
        } catch (e: DeliusValidationError) {
            telemetryService.trackEvent("UpdateRejected", mapOf("reason" to e.message) + message.telemetryProperties())
            throw e
        }
    }

    override fun handle(message: String) {
        try {
            handle(converter.fromMessage(message))
        } catch (e: JsonMappingException) {
            telemetryService.trackEvent("JsonMappingException", mapOf("cause" to message))
        }
    }
}

fun HmppsDomainEvent.assessmentDate() =
    ZonedDateTimeDeserializer.deserialize(additionalInformation["AssessmentDate"] as String)

data class RiskAssessment(val score: Double, val band: String)

data class OgrsScore(val ogrs3Yr1: Int, val ogrs3Yr2: Int)

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

fun HmppsDomainEvent.ogrsScore() = OgrsScore(
    additionalInformation["OGRS3Yr1"] as Int,
    additionalInformation["OGRS3Yr2"] as Int
)

fun HmppsDomainEvent.telemetryProperties() = mapOf(
    "occurredAt" to occurredAt.toString(),
    "crn" to (personReference.findCrn() ?: "unknown"),
    "eventNumber" to (additionalInformation["EventNumber"]?.toString() ?: "Not Provided"),
    "rsrScore" to additionalInformation["RSRScore"].toString(),
    "rsrBand" to additionalInformation["RSRBand"].toString(),
    "ospIndecentScore" to additionalInformation["OSPIndecentScore"].toString(),
    "ospIndecentBand" to additionalInformation["OSPIndecentBand"].toString(),
    "ospContactScore" to additionalInformation["OSPContactScore"].toString(),
    "ospContactBand" to additionalInformation["OSPContactBand"].toString(),
    "OGRS3Yr1" to additionalInformation["OGRS3Yr1"].toString(),
    "OGRS3Yr2" to additionalInformation["OGRS3Yr2"].toString(),
)
