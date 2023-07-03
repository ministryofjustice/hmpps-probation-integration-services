package uk.gov.justice.digital.hmpps.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.ldap.core.ContextSource
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.ldap.pool.factory.PoolingContextSource

@Configuration
@ConditionalOnClass(name = ["org.springframework.ldap.core.support.LdapContextSource"])
class LdapConfig {
    @Bean
    @Primary
    fun ldapPooledContextSource(
        ldapContextSource: LdapContextSource
    ): ContextSource = PoolingContextSource().apply {
        contextSource = ldapContextSource
    }
}
