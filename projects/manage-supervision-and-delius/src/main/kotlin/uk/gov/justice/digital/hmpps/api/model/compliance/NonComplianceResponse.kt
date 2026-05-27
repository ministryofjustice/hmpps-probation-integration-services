package uk.gov.justice.digital.hmpps.api.model.compliance

import uk.gov.justice.digital.hmpps.api.model.CodeAndDescription
import java.time.LocalDate

data class NonComplianceResponse(
    val acceptableAbsence: List<NonComplianceDetail>,
    val unacceptableAbsence: List<NonComplianceDetail>,
    val attendedButDidNotComply: List<NonComplianceDetail>
)

data class NonComplianceDetail(
    val contactId: Long,
    val eventNumber: String,
    val eventId: Long,
    val type: CodeAndDescription,
    val date: LocalDate,
)