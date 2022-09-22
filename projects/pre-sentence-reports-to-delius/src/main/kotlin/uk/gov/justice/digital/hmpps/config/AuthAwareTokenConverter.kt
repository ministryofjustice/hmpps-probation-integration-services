package uk.gov.justice.digital.hmpps.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class AuthAwareTokenConverter : Converter<Jwt, AbstractAuthenticationToken> {
    private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> = JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        return AuthAwareAuthenticationToken(
            jwt = jwt,
            clientOnly = jwt.subject == jwt.claims["client_id"],
            subject = jwt.subject,
            deliusUser = "delius" == jwt.claims["auth_source"],
            authorities = extractAuthorities(jwt)
        )
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>().apply { addAll(jwtGrantedAuthoritiesConverter.convert(jwt)!!) }
        if (jwt.claims.containsKey("authorities")) {
            @Suppress("UNCHECKED_CAST")
            val claimAuthorities = (jwt.claims["authorities"] as Collection<String>).toList()
            authorities.addAll(claimAuthorities.map(::SimpleGrantedAuthority))
        }
        return authorities.toSet()
    }
}

class AuthAwareAuthenticationToken(
    jwt: Jwt,
    val clientOnly: Boolean,
    val deliusUser: Boolean,
    val subject: String,
    authorities: Collection<GrantedAuthority>
) : JwtAuthenticationToken(jwt, authorities)
