package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.user.AuditUser
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager,
) : ApplicationListener<ApplicationReadyEvent> {
    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(AuditUser(id(), "AccreditedProgrammesAndDelius"))
    }

    @Transactional
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        TestData.forEach { entityManager.persist(it) }
    }
}