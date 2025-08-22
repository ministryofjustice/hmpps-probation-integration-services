package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult
import uk.gov.justice.digital.hmpps.integrations.delius.model.properties
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.Alert
import uk.gov.justice.digital.hmpps.integrations.prison.AlertCode
import uk.gov.justice.digital.hmpps.integrations.prison.toDeliusCaseNote
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class PrisonerAlert(
    private val detailService: DomainEventDetailService,
    private val deliusService: DeliusService,
    private val telemetryService: TelemetryService,
) {
    fun handle(event: HmppsDomainEvent) {
        val alert: Alert = try {
            detailService.getDetail(event)
        } catch (_: HttpClientErrorException.NotFound) {
            return telemetryService.trackEvent("AlertNotFound", mapOf("detailUrl" to event.detailUrl!!))
        }

        // ignore OCG Nominal - Do not share
        if (alert.alertCode.code == AlertCode.OCG_NOMINAL) return

        try {
            val result = deliusService.mergeCaseNote(alert.toDeliusCaseNote())
            if (result is MergeResult.Success && result.action is MergeResult.Action.Moved) {
                deliusService.createDataCleanseContact(setOf(result.action.from))
            }
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