package uk.gov.justice.digital.hmpps.audit.service

import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.security.ServiceContext
import java.util.Optional

@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "auditingDateTimeProvider")
@Component(value = "auditorAware")
class AuditorAware : AuditorAware<Long> {
    override fun getCurrentAuditor(): Optional<Long> = Optional.ofNullable(ServiceContext.servicePrincipal()!!.userId)
}
