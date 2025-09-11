package uk.gov.justice.digital.hmpps.audit.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.security.ServiceContext
import java.util.*

@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "auditingDateTimeProvider")
@Component(value = "auditorAware")
@ConditionalOnProperty("use-delius-user", havingValue = "false", matchIfMissing = true, prefix = "service")
class AuditorAware : AuditorAware<Long> {
    override fun getCurrentAuditor(): Optional<Long> = Optional.ofNullable(ServiceContext.servicePrincipal()!!.userId)
}
