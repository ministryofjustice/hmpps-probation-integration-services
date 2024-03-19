package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.ZonedDateTime

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val bir: BusinessInteractionRepository,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        BusinessInteractionCode.entries.forEach {
            bir.save(
                BusinessInteraction(
                    IdGenerator.getAndIncrement(),
                    it.code,
                    ZonedDateTime.now().minusMonths(6)
                )
            )
        }
    }
}
