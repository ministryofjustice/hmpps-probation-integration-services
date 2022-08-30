package uk.gov.justice.digital.hmpps.config.security

import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ConditionalOnDefaultWebSecurity
class SecurityConfig {

    fun filterChain(http: HttpSecurity): HttpSecurity {
        http.authorizeRequests {
            it
                .antMatchers("/health/**", "/info/**", "/hawtio/**", "/jolokia").permitAll()
                .anyRequest().authenticated()
        }
            .csrf().disable()
            .cors().disable()
            .httpBasic().disable()
            .formLogin().disable()
            .logout().disable()
        return http
    }

    @Bean
    fun configure(http: HttpSecurity): SecurityFilterChain = filterChain(http).build()
}
