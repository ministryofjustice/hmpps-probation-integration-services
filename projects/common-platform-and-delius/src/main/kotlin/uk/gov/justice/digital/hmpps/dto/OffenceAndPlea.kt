package uk.gov.justice.digital.hmpps.dto

import uk.gov.justice.digital.hmpps.messaging.Plea
import java.time.LocalDate

data class OffenceAndPlea(
    val offenceCode: String,
    val homeOfficeOffenceCode: String,
    val plea: Plea? = null,
    val startDate: LocalDate? = null,
    val count: Int? = null,
)