package uk.gov.justice.digital.hmpps.api.model.risk

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate

data class RiskFlagRemoval(
    val notes: String?,
    val removalDate: LocalDate,
    val removedBy: Name
)