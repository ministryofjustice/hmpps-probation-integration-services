package uk.gov.justice.digital.hmpps.dto

import uk.gov.justice.digital.hmpps.messaging.Plea

data class OffenceAndPlea(
    val offenceCode: String,
    val homeOfficeOffenceCode: String,
    val plea: Plea?
)