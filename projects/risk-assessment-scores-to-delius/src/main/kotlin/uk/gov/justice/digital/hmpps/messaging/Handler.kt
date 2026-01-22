package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.DeliusValidationError
import uk.gov.justice.digital.hmpps.integrations.delius.RiskAssessmentService
import uk.gov.justice.digital.hmpps.integrations.delius.RiskScoreService
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.flagged.RiskAssessmentService as FlaggedRiskAssessmentService

@Component
class Handler(
    private val telemetryService: TelemetryService,
    private val riskScoreService: RiskScoreService,
    private val riskAssessmentService: RiskAssessmentService,
    private val flaggedRiskAssessmentService: FlaggedRiskAssessmentService,
    private val featureFlags: FeatureFlags,
    override val converter: NotificationConverter<HmppsDomainEvent>
) : NotificationHandler<HmppsDomainEvent> {
    companion object {
        const val DELIUS_OGRS4_SUPPORT = "delius-ogrs4-support"
    }

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
                    if (featureFlags.enabled(DELIUS_OGRS4_SUPPORT)) {
                        riskAssessmentService.addOrUpdateRiskAssessment(
                            message.personReference.findCrn()
                                ?: throw IllegalArgumentException("Missing CRN in ${message.personReference}"),
                            message.additionalInformation["EventNumber"] as Int?,
                            message.assessmentDate(),
                            message.ogrs4Score()
                        )
                    } else {
                        flaggedRiskAssessmentService.addOrUpdateRiskAssessment(
                            message.personReference.findCrn()
                                ?: throw IllegalArgumentException("Missing CRN in ${message.personReference}"),
                            message.additionalInformation["EventNumber"] as Int?,
                            message.assessmentDate(),
                            message.ogrsScore()
                        )
                    }

                    telemetryService.trackEvent("AddOrUpdateRiskAssessment", message.telemetryProperties())
                } catch (dve: DeliusValidationError) {
                    telemetryService.trackEvent(
                        "AddOrUpdateRiskAssessmentRejected",
                        mapOf("reason" to dve.message) + message.telemetryProperties()
                    )
                    if (!dve.ignored()) throw dve
                } catch (e: IgnorableMessageException) {
                    telemetryService.trackEvent(
                        "AddOrUpdateRiskAssessmentRejected",
                        mapOf("reason" to e.message) + message.telemetryProperties()
                    )
                }
            }
        }
    }
}

fun HmppsDomainEvent.assessmentDate() =
    ZonedDateTimeDeserializer.deserialize(additionalInformation["AssessmentDate"] as String)

data class RiskAssessment(val score: Double, val band: String, val staticOrDynamic: String? = null)

data class OgrsScore(val ogrs3Yr1: Int, val ogrs3Yr2: Int)

data class Ogrs4Score(val ogrs3Yr1: Int?, val ogrs3Yr2: Int?, val ogrs4GYr2: Double?, val ogp2Yr2: Double?,
    val ogrs4VYr2: Double?, val ovp2Yr2: Double?, val ogp2Yr2Band: String?, val ogrs4GYr2Band: String?)

fun HmppsDomainEvent.rsr() = RiskAssessment(
    additionalInformation["RSRScore"] as Double,
    additionalInformation["RSRBand"] as String,
    additionalInformation["RSRStaticOrDynamic"] as String
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

fun HmppsDomainEvent.ogrs4Score() = Ogrs4Score(
    additionalInformation["OGRS3Yr1"] as Int?,
    additionalInformation["OGRS3Yr2"] as Int?,
    additionalInformation["OGRS4GYr2"] as Double?,
    additionalInformation["OGP2Yr2"] as Double?,
    additionalInformation["OGRS4VYr2"] as Double?,
    additionalInformation["OVP2Yr2"] as Double?,
    additionalInformation["OGP2Yr2Band"] as String?,
    additionalInformation["OGRS4GYr2Band"] as String?
)

fun HmppsDomainEvent.telemetryProperties() = mapOf(
    "occurredAt" to occurredAt.toString(),
    "crn" to (personReference.findCrn() ?: "unknown"),
    "eventNumber" to (additionalInformation["EventNumber"]?.toString() ?: "Not Provided"),
    "rsrScore" to additionalInformation["RSRScore"].toString(),
    "rsrBand" to additionalInformation["RSRBand"].toString(),
    "rsrStaticOrDynamic" to additionalInformation["RSRStaticOrDynamic"].toString(),
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
