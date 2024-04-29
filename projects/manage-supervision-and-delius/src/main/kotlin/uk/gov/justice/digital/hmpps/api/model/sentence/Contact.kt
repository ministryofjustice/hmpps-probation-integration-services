package uk.gov.justice.digital.hmpps.api.model.sentence

import java.time.LocalDate

class Contact(
    val name: String,
    val email: String?,
    val telephoneNumber: String?,
    val provider: String,
    val probationDeliveryUnit: String,
    val team: String?,
    val allocatedUntil: LocalDate?
)
