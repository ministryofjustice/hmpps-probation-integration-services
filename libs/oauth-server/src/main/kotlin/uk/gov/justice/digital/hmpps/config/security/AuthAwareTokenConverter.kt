package uk.gov.justice.digital.hmpps.config.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class AuthAwareTokenConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        return AuthAwareAuthenticationToken(
            jwt = jwt,
            authorities = extractAuthorities(jwt)
        )
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        if (jwt.claims.containsKey("authorities")) {
            @Suppress("UNCHECKED_CAST")
            return (jwt.claims["authorities"] as Collection<String>).map(::SimpleGrantedAuthority)
        }
        return listOf()
    }
}

class AuthAwareAuthenticationToken(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>
) : JwtAuthenticationToken(jwt, authorities)
