package uk.gov.justice.digital.hmpps.api.model.offence

import uk.gov.justice.digital.hmpps.api.model.Name

data class Offences(
    val name: Name,
    val mainOffence: Offence? = null,
    val additionalOffences: List<Offence>? = listOf()
)
