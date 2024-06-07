package uk.gov.justice.digital.hmpps.config.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl.withDefaultRolePrefix
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

fun interface SecurityConfigurer {
    fun configure(http: HttpSecurity): HttpSecurity
}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnDefaultWebSecurity
@EnableConfigurationProperties(SecurityConfig.OAuth2Properties::class)
class SecurityConfig(private val configurers: List<SecurityConfigurer>) {

    @Bean
    @ConditionalOnMissingBean
    fun configure(http: HttpSecurity): SecurityFilterChain {
        configurers.forEach { it.configure(http) }
        return http.build()
    }

    @Bean
    fun roleHierarchy(oauth2: OAuth2Properties): RoleHierarchy = withDefaultRolePrefix().also {
        if (oauth2.roles.isNotEmpty()) it.role("PROBATION_INTEGRATION_ADMIN").implies(*oauth2.roles.toTypedArray())
    }.build()

    @Bean
    fun methodSecurityExpressionHandler(roleHierarchy: RoleHierarchy) =
        DefaultMethodSecurityExpressionHandler().also { it.setRoleHierarchy(roleHierarchy) }

    @ConfigurationProperties(prefix = "oauth2")
    class OAuth2Properties {
        var roles: List<String> = emptyList()
    }
}
