package uk.gov.justice.digital.hmpps.api.model.sentence

import java.time.LocalDate

data class OffenceDetails(
    val offence: Offence?,
    val dateOfOffence: LocalDate?,
    val notes: String?,
    val additionalOffences: List<Offence>
)
