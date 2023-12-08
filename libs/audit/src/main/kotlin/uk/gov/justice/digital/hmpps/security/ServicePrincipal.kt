package uk.gov.justice.digital.hmpps.security

class ServicePrincipal(
    val username: String,
    val userId: Long,
) {
    companion object {
        const val AUTHORITY = "ROLE_AUTHORISED"
    }
}
