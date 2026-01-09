package uk.gov.justice.digital.hmpps.appointments.model

data class ApplyOutcomeRequest(
    val reference: String,
    val outcome: RequestCode,
    val username: String? = null,
)