package uk.gov.justice.digital.hmpps.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
class CommonSecurityConfigurer : SecurityConfigurer {
    override fun configure(http: HttpSecurity): HttpSecurity {
        http.authorizeHttpRequests {
            it.requestMatchers(
                AntPathRequestMatcher("/actuator/**"),
                AntPathRequestMatcher("/health/**"),
                AntPathRequestMatcher("/info/**"),
                AntPathRequestMatcher("/swagger-ui/**"),
                AntPathRequestMatcher("/v3/api-docs.yaml"),
                AntPathRequestMatcher("/v3/api-docs/**"),
            ).permitAll().anyRequest().authenticated()
        }
            .csrf { it.disable() }
            .cors { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
        return http
    }
}
