package uk.gov.justice.digital.hmpps.api.model.schedule

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate

data class LinkedEnforcementAction(
    val enforcementId: Long,
    val enforcementDescription: String?,
    val enforcementDate: LocalDate?,
    val createdBy: Name?
)
