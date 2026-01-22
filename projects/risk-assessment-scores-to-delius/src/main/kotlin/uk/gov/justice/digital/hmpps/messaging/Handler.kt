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

@Component
class Handler(
    private val telemetryService: TelemetryService,
    private val riskScoreService: RiskScoreService,
    private val riskAssessmentService: RiskAssessmentService,
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val featureFlags: FeatureFlags
) : NotificationHandler<HmppsDomainEvent> {

    val flagValue = featureFlags.enabled("delius-ogrs4-support")
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
                        if ( flagValue ) message.rsr() else message.rsrOLD(),
                        if ( flagValue ) message.ospIndecent() else message.ospIndecentOLD(),
                        if ( flagValue ) message.ospIndirectIndecent() else message.ospIndirectIndecentOLD(),
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

sealed class RiskAssessment {
    abstract val score: Double
    abstract val band: String
    abstract val staticOrDynamic: String?

    data class V3(
        override val score: Double,
        override val band: String,
        override val staticOrDynamic: String? = null
    ) : RiskAssessment()

    data class V4(
        override val score: Double,
        override val band: String,
        override val staticOrDynamic: String? = null,
        val algorithmVersion: String? = null
    ) : RiskAssessment()
}

data class OgrsScore(val ogrs3Yr1: Int, val ogrs3Yr2: Int)


fun HmppsDomainEvent.rsr() =  RiskAssessment.V4(
    additionalInformation["RSRScore"] as Double,
    additionalInformation["RSRBand"] as String,
    additionalInformation["RSRStaticOrDynamic"] as String,
    additionalInformation["RSRAlgorithmVersion"] as String
)

fun HmppsDomainEvent.rsrOLD() =  RiskAssessment.V3(
    additionalInformation["RSRScore"] as Double,
    additionalInformation["RSRBand"] as String,
    additionalInformation["RSRStaticOrDynamic"] as String,
)

fun HmppsDomainEvent.ospIndecent() = additionalInformation["OSPIndecentScore"]?.let {
    RiskAssessment.V4(
        score = additionalInformation["OSPIndecentScore"] as Double,
        band = additionalInformation["OSPIndecentBand"] as String,
        algorithmVersion = additionalInformation["RSRAlgorithmVersion"] as String
    )
}

fun HmppsDomainEvent.ospIndecentOLD() = additionalInformation["OSPIndecentScore"]?.let {
    RiskAssessment.V3(
        score = additionalInformation["OSPIndecentScore"] as Double,
        band = additionalInformation["OSPIndecentBand"] as String,
    )
}


fun HmppsDomainEvent.ospIndirectIndecent() = additionalInformation["OSPIndirectIndecentBand"]?.let {
    RiskAssessment.V4(
        score = additionalInformation["OSPIndirectIndecentScore"] as Double,
        band = additionalInformation["OSPIndirectIndecentBand"] as String,
        algorithmVersion = additionalInformation["RSRAlgorithmVersion"] as String
    )
}

fun HmppsDomainEvent.ospIndirectIndecentOLD() = additionalInformation["OSPIndirectIndecentBand"]?.let {
    RiskAssessment.V3(
        score = additionalInformation["OSPIndirectIndecentScore"] as Double,
        band = additionalInformation["OSPIndirectIndecentBand"] as String,
    )
}


fun HmppsDomainEvent.ospContact() = additionalInformation["OSPContactScore"]?.let {
    RiskAssessment.V3(
        additionalInformation["OSPContactScore"] as Double,
        additionalInformation["OSPContactBand"] as String,
    )
}

fun HmppsDomainEvent.ospDirectContact() = additionalInformation["OSPDirectContactBand"]?.let {
    RiskAssessment.V3(
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
