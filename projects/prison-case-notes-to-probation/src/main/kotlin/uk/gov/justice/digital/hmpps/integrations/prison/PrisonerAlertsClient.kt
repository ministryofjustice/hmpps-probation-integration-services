package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.model.StaffName
import java.net.URI
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

interface PrisonerAlertsClient {
    @GetExchange
    fun getAlert(baseUrl: URI): Alert
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
