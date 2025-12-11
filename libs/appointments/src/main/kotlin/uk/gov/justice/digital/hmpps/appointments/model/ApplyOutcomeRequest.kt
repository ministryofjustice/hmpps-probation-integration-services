package uk.gov.justice.digital.hmpps.appointments.model

data class ApplyOutcomeRequest(
    val externalReference: String,
    val outcome: RequestCode,
    val username: String? = null,
)