package uk.gov.justice.digital.hmpps.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallService
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseService
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.temporal.ChronoUnit.DAYS

@Component
@EnableJms
class MessageListener(
    private val telemetryService: TelemetryService,
    private val releaseService: ReleaseService,
    private val recallService: RecallService,
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(hmppsEvent: HmppsEvent) {
        log.debug("received $hmppsEvent")
        telemetryService.hmppsEventReceived(hmppsEvent)

        try {
            when (hmppsEvent.eventType) {
                "prison-offender-events.prisoner.released" -> {
                    releaseService.release(
                        hmppsEvent.additionalInformation.nomsNumber(),
                        hmppsEvent.additionalInformation.prisonId(),
                        hmppsEvent.additionalInformation.reason(),
                        hmppsEvent.occurredAt.truncatedTo(DAYS),
                    )
                    telemetryService.trackEvent("PrisonerReleased", hmppsEvent.telemetryProperties())
                }

                "prison-offender-events.prisoner.received" -> {
                    recallService.recall(
                        hmppsEvent.additionalInformation.nomsNumber(),
                        hmppsEvent.additionalInformation.prisonId(),
                        hmppsEvent.additionalInformation.reason(),
                        hmppsEvent.occurredAt.truncatedTo(DAYS),
                    )
                    telemetryService.trackEvent("PrisonerRecalled", hmppsEvent.telemetryProperties())
                }

                else -> throw IllegalArgumentException("Unknown event type ${hmppsEvent.eventType}")
            }
        } catch (e: IgnorableMessageException) {
            telemetryService.trackEvent(e.message, hmppsEvent.telemetryProperties() + e.additionalProperties)
            return
        }
    }
}

fun AdditionalInformation.nomsNumber() = this["nomsNumber"] as String
fun AdditionalInformation.prisonId() = this["prisonId"] as String
fun AdditionalInformation.reason() = this["reason"] as String
fun AdditionalInformation.details() = this["details"] as String?
fun HmppsEvent.telemetryProperties() = listOfNotNull(
    "occurredAt" to occurredAt.toString(),
    "nomsNumber" to additionalInformation.nomsNumber(),
    "institution" to additionalInformation.prisonId(),
    "reason" to additionalInformation.reason(),
    additionalInformation.details()?.let { "details" to it }
).toMap()
