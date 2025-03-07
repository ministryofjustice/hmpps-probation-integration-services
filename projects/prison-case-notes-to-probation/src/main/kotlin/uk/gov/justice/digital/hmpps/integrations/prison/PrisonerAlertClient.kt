package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteBody
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader.Type.ActiveAlert
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader.Type.InactiveAlert
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.model.StaffName
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime.of
import java.time.ZonedDateTime
import java.util.*

interface PrisonerAlertClient {
    @GetExchange
    fun getAlert(baseUrl: URI): Alert

    @GetExchange
    fun getActiveAlerts(uri: URI): Alerts
}

data class Alert(
    val alertUuid: UUID,
    val prisonNumber: String,
    val alertCode: AlertCode,
    val description: String?,
    val authorisedBy: String?,
    val activeFrom: LocalDate,
    val activeTo: LocalDate?,
    val isActive: Boolean,
    val createdAt: ZonedDateTime,
    val createdBy: String,
    val createdByDisplayName: String,
    val lastModifiedAt: ZonedDateTime?,
    val lastModifiedBy: String?,
    val lastModifiedByDisplayName: String?,
    val activeToLastSetAt: ZonedDateTime?,
    val prisonCodeWhenCreated: String?
) {
    fun staffName(): StaffName {
        val name = lastModifiedByDisplayName ?: createdByDisplayName
        return StaffName(name.substringBeforeLast(" ").trim(), name.substringAfterLast(" ").trim())
    }
}

data class AlertCode(
    val alertTypeCode: String,
    val alertTypeDescription: String,
    val code: String,
    val description: String,
)

data class Alerts(val content: List<Alert>)

fun Alert.toDeliusCaseNote(): DeliusCaseNote {
    return DeliusCaseNote(
        header = CaseNoteHeader(prisonNumber, null, alertUuid, if (isActive) ActiveAlert else InactiveAlert),
        body = CaseNoteBody(
            type = "ALERT",
            subType = if (isActive) "ACTIVE" else "INACTIVE",
            content = description ?: "",
            contactTimeStamp = ZonedDateTime.of(
                of(
                    if (isActive) activeFrom else activeTo,
                    listOfNotNull(
                        activeToLastSetAt,
                        lastModifiedAt,
                        createdAt
                    ).max().toLocalTime()
                ),
                EuropeLondon
            ),
            systemTimestamp = lastModifiedAt ?: createdAt,
            staffName = staffName(),
            establishmentCode = checkNotNull(prisonCodeWhenCreated) {
                "Unable to verify establishment for alert"
            },
            "Alert ${alertCode.alertTypeDescription} and ${alertCode.description} made ${if (isActive) "active" else "inactive"}."
        )
    )
}