package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.overview.Disability
import java.time.LocalDate

data class Provisions(
    val provisions: List<String>,
    val lastUpdated: LocalDate?
)