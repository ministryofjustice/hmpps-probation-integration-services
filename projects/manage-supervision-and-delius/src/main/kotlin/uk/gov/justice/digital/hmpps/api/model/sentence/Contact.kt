package uk.gov.justice.digital.hmpps.api.model.sentence

import java.time.LocalDate

data class Contact(
    val name: String,
    val email: String?,
    val telephoneNumber: String?,
    val provider: String,
    val probationDeliveryUnit: String,
    val team: String?,
    val allocationDate: LocalDate,
    val allocatedUntil: LocalDate?,
    val responsibleOfficer: Boolean,
    val prisonOffenderManager: Boolean
)
