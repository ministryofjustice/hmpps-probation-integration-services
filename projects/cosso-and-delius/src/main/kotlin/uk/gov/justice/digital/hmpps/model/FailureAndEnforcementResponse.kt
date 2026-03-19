package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class FailureAndEnforcementResponse(
    val enforceableContacts: List<ContactResponse>,
    val registrations: List<RegistrationResponse>
)

data class ContactResponse(
    val id: Long,
    val datetime: ZonedDateTime,
    val description: String,
    val type: CodeAndDescription,
    val outcome: CodeAndDescription,
    val notes: String,
)

data class RegistrationResponse(
    val id: Long,
    val type: CodeAndDescription,
    val level: CodeAndDescription,
    val category: CodeAndDescription,
    val startDate: LocalDate,
    val endData: LocalDate?,
    val notes: String?,
    val documentsLinked: Boolean,
    val deregistered: Boolean,
)