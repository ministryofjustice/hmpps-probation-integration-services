package uk.gov.justice.digital.hmpps.api.model.schedule

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate

data class LinkedContact(
    val contactId: Long,
    val contactTypeDescription: String,
    val contactDate: LocalDate,
    val createdBy: Name?
)
