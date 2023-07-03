package uk.gov.justice.digital.hmpps.config

import org.springframework.boot.autoconfigure.ldap.LdapProperties
import org.springframework.boot.context.properties.PropertyMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.ldap.pool.factory.PoolingContextSource

@Configuration
class LdapConfig {
    @Bean
    fun poolingContextSource(
        ldapContextSource: LdapContextSource
    ): PoolingContextSource = PoolingContextSource().apply {
        contextSource = ldapContextSource
    }

    @Bean
    fun ldapTemplate(properties: LdapProperties, poolingContextSource: PoolingContextSource): LdapTemplate {
        val template = properties.template
        val propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull()
        val ldapTemplate = LdapTemplate(poolingContextSource)
        propertyMapper.from(template.isIgnorePartialResultException)
            .to { ldapTemplate.setIgnorePartialResultException(it) }
        propertyMapper.from(template.isIgnoreNameNotFoundException)
            .to { ldapTemplate.setIgnoreNameNotFoundException(it) }
        propertyMapper.from(template.isIgnoreSizeLimitExceededException)
            .to { ldapTemplate.setIgnoreSizeLimitExceededException(it) }
        return ldapTemplate
    }
}
