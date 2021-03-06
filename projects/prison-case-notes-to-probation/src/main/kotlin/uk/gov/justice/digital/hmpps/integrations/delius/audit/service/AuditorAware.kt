package uk.gov.justice.digital.hmpps.integrations.delius.audit.service

import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.security.ServicePrincipal
import java.util.Optional

@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "auditingDateTimeProvider")
@Component(value = "auditorAware")
class AuditorAware : AuditorAware<Long> {
    override fun getCurrentAuditor(): Optional<Long> {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return if (principal is ServicePrincipal) {
            Optional.ofNullable(principal.userId)
        } else Optional.empty()
    }
}
