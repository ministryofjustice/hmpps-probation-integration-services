package uk.gov.justice.digital.hmpps.api.model.contact

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate

data class EnforcementContactResponse(
    val size: Int,
    val page: Int,
    val totalResults: Int,
    val totalPages: Int,
    val enforcementContacts: List<EnforcementContactItem>
)

data class EnforcementContactItem(
    val caseName: Name,
    val id: Long,
    val crn: String,
    val dob: LocalDate,
    val appointmentType: String,
    val appointmentDate: LocalDate,
    val appointmentOutcome: String?,
    val enforcementAction: String?,
    val evidenceDueDate: LocalDate?,
    val deliusManaged: Boolean
)
