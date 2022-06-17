package uk.gov.justice.digital.hmpps.listener

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class ServicePrincipal(
    @Value("\${spring.security.oauth2.client.registration.case-notes.client-id}") private val clientId: String
) : UserDetails {
    companion object {
        const val AUTHORITY = "ROLE_CASE_NOTES"
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableListOf(GrantedAuthority { AUTHORITY })

    override fun getPassword(): String = ""

    override fun getUsername(): String = clientId

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}