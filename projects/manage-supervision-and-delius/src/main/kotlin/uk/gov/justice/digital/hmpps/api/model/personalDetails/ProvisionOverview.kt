package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.PersonSummary
import java.time.LocalDate

data class ProvisionOverview(
    val personSummary: PersonSummary,
    val provisions: List<Provision>,

    )

data class Provision(
    val description: String,
    val notes: String?,
    val startDate: LocalDate,
    val lastUpdated: LocalDate,
    val lastUpdatedBy: Name
)