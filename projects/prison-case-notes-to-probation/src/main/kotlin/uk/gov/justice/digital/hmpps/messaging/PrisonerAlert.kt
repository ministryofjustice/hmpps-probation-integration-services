package uk.gov.justice.digital.hmpps.messaging

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.model.properties
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.Alert
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonerAlertClient
import uk.gov.justice.digital.hmpps.integrations.prison.toDeliusCaseNote
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Service
class PrisonerAlert(
    private val alertsClient: PrisonerAlertClient,
    private val deliusService: DeliusService,
    private val telemetryService: TelemetryService,
    private val featureFlags: FeatureFlags,
) {
    fun handle(event: HmppsDomainEvent) {
        if (!featureFlags.enabled("alert-case-notes-from-alerts-api")) return

        val alert: Alert = try {
            alertsClient.getAlert(URI.create(event.detailUrl!!))
        } catch (ex: HttpStatusCodeException) {
            when (ex.statusCode) {
                HttpStatus.NOT_FOUND -> {
                    telemetryService.trackEvent("AlertNotFound", mapOf("detailUrl" to event.detailUrl!!))
                    return
                }

                else -> throw ex
            }
        }

        try {
            val result = deliusService.mergeCaseNote(alert.toDeliusCaseNote())
            telemetryService.trackEvent("AlertMerged", alert.properties() + result.properties())
        } catch (e: Exception) {
            telemetryService.trackEvent(
                "AlertMergeFailed",
                alert.properties() + ("exception" to (e.message ?: ""))
            )
            if (e !is OffenderNotFoundException) throw e
        }
    }

    private fun Alert.properties(): Map<String, String> = listOfNotNull(
        "alertUuid" to alertUuid.toString(),
        "type" to alertCode.alertTypeCode,
        "subType" to alertCode.code,
        "created" to DeliusDateTimeFormatter.format(createdAt),
        "activeFrom" to DeliusDateFormatter.format(activeFrom),
        activeTo?.let { "activeTo" to DeliusDateFormatter.format(it) },
    ).toMap()
}