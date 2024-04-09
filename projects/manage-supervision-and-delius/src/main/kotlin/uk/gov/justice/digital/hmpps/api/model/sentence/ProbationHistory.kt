package uk.gov.justice.digital.hmpps.api.model.sentence

import java.time.LocalDate

data class ProbationHistory(
    val numberOfTerminatedEvents: Int,
    val dateOfMostRecentTerminatedEvent: LocalDate?,
    val numberOfTerminatedEventBreaches: Int,
    val numberOfProfessionalContacts: Long
)
