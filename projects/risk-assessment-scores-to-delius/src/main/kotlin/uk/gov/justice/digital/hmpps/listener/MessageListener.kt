package uk.gov.justice.digital.hmpps.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.integrations.delius.RiskScoreService
import uk.gov.justice.digital.hmpps.message.HmppsEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@EnableJms
class MessageListener(
    private val telemetryService: TelemetryService,
    private val riskScoreService: RiskScoreService,
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(hmppsEvent: HmppsEvent) {
        log.debug("received $hmppsEvent")
        telemetryService.hmppsEventReceived(hmppsEvent)
        when (hmppsEvent.eventType) {
            "risk-assessment.scores.rsr.determined" -> {
                riskScoreService.updateRsrScores(
                    hmppsEvent.personReference.findCrn() ?: throw IllegalArgumentException("Missing CRN in ${hmppsEvent.personReference}"),
                    hmppsEvent.additionalInformation["EventNumber"] as Int,
                    ZonedDateTimeDeserializer.deserialize(hmppsEvent.additionalInformation["AssessmentDate"] as String),
                    hmppsEvent.additionalInformation["RSRScore"] as Double,
                    hmppsEvent.additionalInformation["RSRBand"] as String,
                    hmppsEvent.additionalInformation["OSPIndecentScore"] as Double,
                    hmppsEvent.additionalInformation["OSPIndecentBand"] as String,
                    hmppsEvent.additionalInformation["OSPContactScore"] as Double,
                    hmppsEvent.additionalInformation["OSPContactBand"] as String,
                )
                telemetryService.trackEvent("RsrScoresUpdated", hmppsEvent.telemetryProperties())
            }
            "risk-assessment.scores.ogrs.determined" ->
                telemetryService.trackEvent("UnsupportedEventType", hmppsEvent.telemetryProperties())
            else -> throw IllegalArgumentException("Unexpected event type ${hmppsEvent.eventType}")
        }
    }
}

fun HmppsEvent.telemetryProperties() = listOfNotNull(
    "occurredAt" to occurredAt.toString(),
    "personReference" to personReference.toString(),
    "additionalInformation" to additionalInformation.toString(),
).toMap()
