package uk.gov.justice.digital.hmpps.model

data class ProbationCase(
    val name: String,
    val crn: String,
    val mobileNumber: String?,
    val manager: Manager?,
    val probationDeliveryUnit: String,
)