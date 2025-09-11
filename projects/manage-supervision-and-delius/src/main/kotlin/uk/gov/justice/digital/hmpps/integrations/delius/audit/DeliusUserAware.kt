package uk.gov.justice.digital.hmpps.integrations.delius.audit

import org.springframework.context.annotation.Primary
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.aspect.UserContext
import uk.gov.justice.digital.hmpps.security.ServiceContext
import java.util.*

@Primary
@EnableJpaAuditing(auditorAwareRef = "deliusUserAware", dateTimeProviderRef = "auditingDateTimeProvider")
@Component(value = "deliusUserAware")
class DeliusUserAware : AuditorAware<Long> {
    override fun getCurrentAuditor(): Optional<Long> =
        Optional.ofNullable(UserContext.get()?.userId ?: ServiceContext.servicePrincipal()!!.userId)
}