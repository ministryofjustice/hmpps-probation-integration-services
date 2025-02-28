package uk.gov.justice.digital.hmpps.messaging

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteBody
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader.Type.ActiveAlert
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader.Type.InactiveAlert
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.Alert
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonerAlertsClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI
import java.time.LocalDateTime.of
import java.time.ZonedDateTime

@Service
class PrisonerAlert(
    private val alertsClient: PrisonerAlertsClient,
    private val deliusService: DeliusService,
    private val telemetryService: TelemetryService,
) {
    fun handle(event: HmppsDomainEvent) {
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
            deliusService.mergeCaseNote(alert.toDeliusCaseNote())
            telemetryService.trackEvent("AlertMerged", alert.properties())
        } catch (e: Exception) {
            telemetryService.trackEvent(
                "AlertMergeFailed",
                alert.properties() + ("exception" to (e.message ?: ""))
            )
            if (e !is OffenderNotFoundException) throw e
        }
    }

    fun Alert.toDeliusCaseNote(): DeliusCaseNote {
        return DeliusCaseNote(
            header = CaseNoteHeader(prisonNumber, null, alertUuid, if (isActive) ActiveAlert else InactiveAlert),
            body = CaseNoteBody(
                type = "ALERT",
                subType = if (isActive) "ACTIVE" else "INACTIVE",
                content = description ?: "",
                contactTimeStamp = ZonedDateTime.of(
                    of(if (isActive) activeFrom else activeTo, createdAt.toLocalTime()),
                    EuropeLondon
                ),
                systemTimestamp = lastModifiedAt ?: createdAt,
                staffName = staffName(),
                establishmentCode = checkNotNull(prisonCodeWhenCreated) {
                    "Unable to verify establishment for alert"
                }
            )
        )
    }

    private fun Alert.properties(): Map<String, String> = mapOf(
        "alertUuid" to alertUuid.toString(),
        "type" to alertCode.alertTypeCode,
        "subType" to alertCode.code,
        "created" to DeliusDateTimeFormatter.format(createdAt),
        "occurrence" to DeliusDateTimeFormatter.format(activeFrom),
    )
}