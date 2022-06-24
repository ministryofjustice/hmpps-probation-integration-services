package uk.gov.justice.digital.hmpps.integrations.delius.audit.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import java.time.ZonedDateTime
import java.util.Optional

@Configuration
class AuditingConfig {
    @Bean
    fun auditingDateTimeProvider() = DateTimeProvider { Optional.of(ZonedDateTime.now()) }
}
