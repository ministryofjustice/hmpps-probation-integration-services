package uk.gov.justice.digital.hmpps.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@EnableJms
class MessageListener(
    private val telemetryService: TelemetryService
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @JmsListener(destination = "\${spring.jms.template.default-destination}")
    fun receive(hmppsEvent: HmppsEvent) {
        log.info("received $hmppsEvent")
        telemetryService.trackEvent(
            "PSR_COMPLETED_EVENT_RECEIVED",
            mapOf(
                "detailUrl" to hmppsEvent.detailUrl
            ) + hmppsEvent.personReference.identifiers.associate { Pair(it.type, it.value) }
        )

       //TODO process the message
    }
}
