package uk.gov.justice.digital.hmpps.api.model.compliance

import java.time.LocalDate

data class Breach(
    val startDate: LocalDate?,
    val status: String?
)
