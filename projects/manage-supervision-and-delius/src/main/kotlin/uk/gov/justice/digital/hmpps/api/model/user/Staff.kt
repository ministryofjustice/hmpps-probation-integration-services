package uk.gov.justice.digital.hmpps.api.model.user

import uk.gov.justice.digital.hmpps.api.model.Name

data class Staff(
    val name: Name,
    val code: String
)
