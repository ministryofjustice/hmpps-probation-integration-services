package uk.gov.justice.digital.hmpps.api.model.user

data class UserDetails(
    val userId: Long,
    val username: String,
    val firstName: String,
    val surname: String,
    val email: String?,
    val enabled: Boolean,
    val roles: List<String>,
    val staff: StaffDetails?
)

data class StaffDetails(
    val probationDeliveryUnits: List<ProbationDeliveryUnit>
)

data class ProbationDeliveryUnit(
    val code: String,
    val description: String
)