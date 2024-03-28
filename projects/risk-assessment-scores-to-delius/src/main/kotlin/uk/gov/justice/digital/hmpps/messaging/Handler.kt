package uk.gov.justice.digital.hmpps.messaging

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
        when (message.eventType) {
            "risk-assessment.scores.rsr.determined" -> {
                try {
                    riskScoreService.updateRsrAndOspScores(
                        message.personReference.findCrn()
                            ?: throw IllegalArgumentException("Missing CRN in ${message.personReference}"),
                        message.additionalInformation["EventNumber"] as Int?,
                        message.assessmentDate(),
                        message.rsr(),
                        message.ospIndecent(),
                        message.ospIndirectIndecent(),
                        message.ospContact(),
                        message.ospDirectContact(),
                    )
                    telemetryService.trackEvent("RsrScoresUpdated", message.telemetryProperties())
                } catch (e: DeliusValidationError) {
                    telemetryService.trackEvent(
                        "RsrUpdateRejected",
                        mapOf("reason" to e.message) + message.telemetryProperties()
                    )
                    if (!e.ignored()) throw e
                }
            }

            "risk-assessment.scores.ogrs.determined" -> {
                try {
                    // if the message doesn't contain the event number then the event is coming from the prison side
                    // so ignore the message
                    if (!message.additionalInformation.containsKey("EventNumber")) {
                        telemetryService.trackEvent(
                            "AddOrUpdateRiskAssessment - ignored due to no event number",
                            message.telemetryProperties()
                        )
                        return
                    } else if (message.additionalInformation["EventNumber"] == null) {
                        // validate that the event number is present
                        telemetryService.trackEvent(
                            "Event number not present",
                            mapOf("crn" to message.personReference.findCrn()!!)
                        )
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
                } catch (dve: DeliusValidationError) {
                    telemetryService.trackEvent(
                        "AddOrUpdateRiskAssessmentRejected",
                        mapOf("reason" to dve.message) + message.telemetryProperties()
                    )
                    if (!dve.ignored()) throw dve
                }
            }
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

fun HmppsDomainEvent.ospIndecent() = additionalInformation["OSPIndecentScore"]?.let {
    RiskAssessment(
        additionalInformation["OSPIndecentScore"] as Double,
        additionalInformation["OSPIndecentBand"] as String,
    )
}

fun HmppsDomainEvent.ospIndirectIndecent() = additionalInformation["OSPIndirectIndecentBand"]?.let {
    RiskAssessment(
        additionalInformation["OSPIndirectIndecentScore"] as Double,
        additionalInformation["OSPIndirectIndecentBand"] as String,
    )
}

fun HmppsDomainEvent.ospContact() = additionalInformation["OSPContactScore"]?.let {
    RiskAssessment(
        additionalInformation["OSPContactScore"] as Double,
        additionalInformation["OSPContactBand"] as String,
    )
}

fun HmppsDomainEvent.ospDirectContact() = additionalInformation["OSPDirectContactBand"]?.let {
    RiskAssessment(
        additionalInformation["OSPDirectContactScore"] as Double,
        additionalInformation["OSPDirectContactBand"] as String,
    )
}

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
    "ospIndirectIndecentScore" to additionalInformation["OSPIndirectIndecentScore"].toString(),
    "ospIndirectIndecentBand" to additionalInformation["OSPIndirectIndecentBand"].toString(),
    "ospDirectContactScore" to additionalInformation["OSPDirectContactScore"].toString(),
    "ospDirectContactBand" to additionalInformation["OSPDirectContactBand"].toString(),
    "OGRS3Yr1" to additionalInformation["OGRS3Yr1"].toString(),
    "OGRS3Yr2" to additionalInformation["OGRS3Yr2"].toString()
)
