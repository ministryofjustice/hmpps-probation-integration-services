package uk.gov.justice.digital.hmpps.integrations.delius.audit

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.service.UserService
import java.time.ZonedDateTime
import java.util.Optional
import java.util.Optional.of

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "auditingDateTimeProvider")
@Service(value = "auditorAware")
class AuditorAwareConfiguration(val userService: UserService) : AuditorAware<Long> {
    override fun getCurrentAuditor(): Optional<Long> {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return if (principal is UserDetails) {
            Optional.ofNullable(userService.findServiceUser(principal.username)?.id)
        } else Optional.empty()
    }

    @Bean
    fun auditingDateTimeProvider() = DateTimeProvider { of(ZonedDateTime.now()) }
}
