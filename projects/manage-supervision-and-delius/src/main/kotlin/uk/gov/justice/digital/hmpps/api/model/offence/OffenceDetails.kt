package uk.gov.justice.digital.hmpps.api.model.offence

import uk.gov.justice.digital.hmpps.api.model.Name

data class OffenceDetails(
    val name: Name,
    val mainOffence: Offence?,
    val mainOffenceNotes: String?,
    val additionalOffences: List<Offence>? = emptyList()
)
