package uk.gov.justice.digital.hmpps.config.security

class ServicePrincipal(
    val username: String,
    val userId: Long?
) {
    companion object {
        const val AUTHORITY = "ROLE_AUTHORISED"
    }
}
