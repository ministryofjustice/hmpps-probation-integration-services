package uk.gov.justice.digital.hmpps.integrations.delius.audit

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.aspect.UserContext
import uk.gov.justice.digital.hmpps.security.ServiceContext
import java.util.*

@ConditionalOnProperty("use-delius-user", havingValue = "true", matchIfMissing = false, prefix = "service")
@EnableJpaAuditing(auditorAwareRef = "deliusUserAware", dateTimeProviderRef = "auditingDateTimeProvider")
@Component(value = "deliusUserAware")
class DeliusUserAware : AuditorAware<Long> {
    override fun getCurrentAuditor(): Optional<Long> =
        Optional.ofNullable(UserContext.get()?.userId ?: ServiceContext.servicePrincipal()!!.userId)
}