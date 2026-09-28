package uk.gov.justice.digital.hmpps.api.model.user

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.ZonedDateTime

data class UserUpdated(
    val username: String,
    val name: Name,
    val updatedDateTime: ZonedDateTime
)
