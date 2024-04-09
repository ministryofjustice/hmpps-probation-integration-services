package uk.gov.justice.digital.hmpps.api.model.sentence

data class ProbationHistory(
    val numberOfTerminatedEvents: Int,
    val numberOfTerminatedEventBreaches: Int,
    val numberOfProfessionalContacts: Long
)
