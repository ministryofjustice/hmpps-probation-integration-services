package uk.gov.justice.digital.hmpps.config.security

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

fun interface SecurityConfigurer {
    fun configure(http: HttpSecurity): HttpSecurity
}

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@ConditionalOnDefaultWebSecurity
class SecurityConfig(private val configurers: List<SecurityConfigurer>) {
    @Bean
    @ConditionalOnMissingBean
    fun configure(http: HttpSecurity): SecurityFilterChain {
        configurers.forEach { it.configure(http) }
        return http.build()
    }
}
