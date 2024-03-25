package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import java.time.LocalDate

data class CircumstanceOverview(
    val personSummary: PersonSummary,
    val circumstances: List<Circumstance>
)

data class Circumstance(
    val type: String,
    val subType: String,
    val notes: String?,
    val verified: Boolean?,
    val startDate: LocalDate,
    val lastUpdated: LocalDate,
    val lastUpdatedBy: Name
)