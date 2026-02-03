package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
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
) : NotificationHandler<HmppsDomainEvent> {

    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val message = notification.message
        val flagValue = message.additionalInformation["RSRAlgorithmVersion"] != null
        when (message.eventType) {
            "risk-assessment.scores.rsr.determined" -> {
                try {
                    riskScoreService.updateRsrAndOspScores(
                        message.personReference.findCrn()
                            ?: throw IllegalArgumentException("Missing CRN in ${message.personReference}"),
                        message.additionalInformation["EventNumber"] as Int?,
                        message.assessmentDate(),
                        if (flagValue) message.rsr() else message.rsrOLD(),
                        if (flagValue) message.ospIndecent() else message.ospIndecentOLD(),
                        if (flagValue) message.ospIndirectIndecent() else message.ospIndirectIndecentOLD(),
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
                        message.ogrs4Score()
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
    abstract val band: String?
    abstract val staticOrDynamic: String?

    data class V3(
        override val score: Double,
        override val band: String?,
        override val staticOrDynamic: String? = null
    ) : RiskAssessment()

    data class V4(
        override val score: Double,
        override val band: String?,
        override val staticOrDynamic: String? = null,
        val algorithmVersion: Int? = null
    ) : RiskAssessment()
}

data class Ogrs4Score(
    val ogrs3Yr1: Int?, val ogrs3Yr2: Int?, val ogrs4GYr2: Double?, val ogp2Yr2: Double?,
    val ogrs4VYr2: Double?, val ovp2Yr2: Double?, val ogp2Yr2Band: String?, val ogrs4GYr2Band: String?
)

fun HmppsDomainEvent.rsr() = RiskAssessment.V4(
    additionalInformation["RSRScore"] as Double,
    mapBand(additionalInformation["RSRBand"] as String?),
    additionalInformation["RSRStaticOrDynamic"] as String,
    additionalInformation["RSRAlgorithmVersion"] as Int
)

fun HmppsDomainEvent.rsrOLD() = RiskAssessment.V3(
    additionalInformation["RSRScore"] as Double,
    mapBand(additionalInformation["RSRBand"]),
    additionalInformation["RSRStaticOrDynamic"] as String,
)

fun HmppsDomainEvent.ospIndecent() = additionalInformation["OSPIndecentScore"]?.let {
    RiskAssessment.V4(
        score = additionalInformation["OSPIndecentScore"] as Double,
        band = mapBand(additionalInformation["OSPIndecentBand"]),
        algorithmVersion = additionalInformation["RSRAlgorithmVersion"] as Int
    )
}

fun HmppsDomainEvent.ospIndecentOLD() = additionalInformation["OSPIndecentScore"]?.let {
    RiskAssessment.V3(
        score = additionalInformation["OSPIndecentScore"] as Double,
        band = mapBand(additionalInformation["OSPIndecentBand"]),
    )
}

fun HmppsDomainEvent.ospIndirectIndecent() = additionalInformation["OSPIndirectIndecentBand"]?.let {
    RiskAssessment.V4(
        score = additionalInformation["OSPIndirectIndecentScore"] as Double,
        band = mapBand(additionalInformation["OSPIndirectIndecentBand"]),
        algorithmVersion = additionalInformation["RSRAlgorithmVersion"] as Int
    )
}

fun HmppsDomainEvent.ospIndirectIndecentOLD() = additionalInformation["OSPIndirectIndecentBand"]?.let {
    RiskAssessment.V3(
        score = additionalInformation["OSPIndirectIndecentScore"] as Double,
        band = mapBand(additionalInformation["OSPIndirectIndecentBand"]),
    )
}

fun HmppsDomainEvent.ospContact() = additionalInformation["OSPContactScore"]?.let {
    RiskAssessment.V3(
        additionalInformation["OSPContactScore"] as Double,
        mapBand(additionalInformation["OSPContactBand"]),
    )
}

fun HmppsDomainEvent.ospDirectContact() = additionalInformation["OSPDirectContactBand"]?.let {
    RiskAssessment.V3(
        additionalInformation["OSPDirectContactScore"] as Double,
        mapBand(additionalInformation["OSPDirectContactBand"]),
    )
}

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

private fun mapBand(band: Any?) = when ((band as String?)?.uppercase()) {
    "LOW", "L" -> "L"
    "MEDIUM", "M" -> "M"
    "HIGH", "H" -> "H"
    "VERY HIGH", "V" -> "V"
    else -> null
}

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
    "OGRS3Yr2" to additionalInformation["OGRS3Yr2"].toString(),
    "RSRAlgorithmVersion" to additionalInformation["RSRAlgorithmVersion"].toString(),
)
