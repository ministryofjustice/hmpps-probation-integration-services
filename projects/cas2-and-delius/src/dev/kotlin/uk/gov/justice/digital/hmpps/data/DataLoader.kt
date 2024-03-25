package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.EntityGenerator.BUSINESS_INTERACTION
import uk.gov.justice.digital.hmpps.data.generator.EntityGenerator.CONTACT_TYPES
import uk.gov.justice.digital.hmpps.data.generator.EntityGenerator.MANAGER
import uk.gov.justice.digital.hmpps.data.generator.EntityGenerator.PERSON
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(applicationReadyEvent: ApplicationReadyEvent) {
        listOf(
            BUSINESS_INTERACTION,
            PERSON,
            MANAGER,
            *CONTACT_TYPES,
        ).forEach {
            entityManager.persist(it)
        }
    }
}
